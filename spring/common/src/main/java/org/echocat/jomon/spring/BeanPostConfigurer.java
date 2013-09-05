/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2013 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import static java.nio.charset.Charset.forName;
import static org.echocat.jomon.spring.BeanPostConfigurer.PropertyValueType.reference;
import static org.echocat.jomon.spring.BeanPostConfigurer.PropertyValueType.value;

public class BeanPostConfigurer implements BeanFactoryPostProcessor, PriorityOrdered {

    private Properties _properties;

    private Charset _charset = forName("UTF-8");
    private Properties _setProperties;
    private Resource _propertiesFile;

    private Properties _setBySystemProperties;
    private Properties _setByResource;

    public BeanPostConfigurer() throws Exception {
        _properties = createNewProperties();
    }

    @Nonnull
    protected Properties createNewProperties() throws Exception {
        final Properties result = new Properties();
        if (_setProperties != null) {
            for (Entry<Object, Object> keyAndValue : _setProperties.entrySet()) {
                final Object key = keyAndValue.getKey();
                final Object value = keyAndValue.getValue();
                if (key != null && value != null) {
                    result.put(key, value);
                }
            }
        }
        if (_propertiesFile != null && _propertiesFile.exists()) {
            _setByResource = new Properties();
            try (final InputStream is = _propertiesFile.getInputStream()) {
                try (final Reader reader = new InputStreamReader(is, _charset)) {
                    result.load(reader);
                    _setByResource.putAll(result);
                }
            }
        }

        _setBySystemProperties = new Properties();
        // noinspection unchecked, RedundantCast
        for (Entry<String, String> keyAndValue : ((Map<String, String>)(Object)System.getProperties()).entrySet()) {
            final String key = keyAndValue.getKey();
            if (key.contains("#") || key.contains("@")) {
                result.put(key, keyAndValue.getValue());
                _setBySystemProperties.setProperty(key, keyAndValue.getValue());
            }
        }
        return result;
    }

    public void setPropertiesFrom(Resource propertiesFile) throws Exception {
        _propertiesFile = propertiesFile;
        _properties = createNewProperties();
    }

    public void setPropertiesFromFileIfExists(File propertiesFile) throws Exception {
        _propertiesFile = propertiesFile != null ? new FileSystemResource(propertiesFile) : null;
        _properties = createNewProperties();
    }

    public void setProperties(Properties properties) throws Exception {
        _setProperties = properties;
        _properties = createNewProperties();
    }

    @Nullable
    public Properties getSetBySystemProperties() {
        return _setBySystemProperties;
    }

    @Nullable
    public Properties getSetByResource() {
        return _setByResource;
    }

    public void setCharset(Charset charset) throws Exception {
        _charset = charset != null ? charset : forName("UTF-8");
        _properties = createNewProperties();
    }

    @Override
    public void postProcessBeanFactory(@Nonnull ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // noinspection unchecked, RedundantCast
        for (Entry<String, String> keyAndValue : ((Map<String, String>)(Object)_properties).entrySet()) {
            handleProperty(keyAndValue.getKey(), keyAndValue.getValue(), beanFactory);
        }
    }

    protected void handleProperty(@Nonnull String key, @Nullable String value, @Nonnull ConfigurableListableBeanFactory beanFactory) throws BeansException {
        final BeanAndPropertyName beanAndPropertyName = getBeanAndPropertyNameOf(key);
        for (AbstractBeanDefinition beanDefinition : getAllBeanDefinitionsBy(beanAndPropertyName.getBeanName(), beanFactory)) {
            handleProperty(beanAndPropertyName, beanDefinition, value, beanAndPropertyName.getPropertyValueType(), beanFactory);
        }
    }

    @Nonnull
    protected List<AbstractBeanDefinition> getAllBeanDefinitionsBy(@Nonnull String targetBeanDefinitionName, @Nonnull ConfigurableListableBeanFactory beanFactory) {
        final List<AbstractBeanDefinition> result = new ArrayList<>();
        for (String beanDefinitionName : beanFactory.getBeanDefinitionNames()) {
            if (targetBeanDefinitionName.equals(beanDefinitionName) || beanDefinitionName.startsWith(targetBeanDefinitionName + "#")) {
                result.add((AbstractBeanDefinition) beanFactory.getBeanDefinition(beanDefinitionName));
            } else {
                for (String alias : beanFactory.getAliases(beanDefinitionName)) {
                    if (targetBeanDefinitionName.equals(alias)) {
                        result.add((AbstractBeanDefinition) beanFactory.getBeanDefinition(beanDefinitionName));
                    }
                }
            }
        }
        return result;
    }


    protected void handleProperty(@Nonnull BeanAndPropertyName beanAndPropertyName, @Nonnull AbstractBeanDefinition beanDefinition, @Nullable String value, @Nonnull PropertyValueType propertyValueType, @Nonnull ConfigurableListableBeanFactory beanFactory) {
        final MutablePropertyValues propertyValues = getPropertyValues(beanDefinition);
        if (propertyValueType != reference || beanFactory.containsBean(value)) {
            removeOldPropertyIfNeeded(beanAndPropertyName, propertyValues);
            final PropertyValue propertyValue = new PropertyValue(beanAndPropertyName.getPropertyName(), propertyValueType == reference ? new RuntimeBeanReference(value): value);
            propertyValues.addPropertyValue(propertyValue);
        }
    }

    @Nonnull
    protected MutablePropertyValues getPropertyValues(@Nonnull AbstractBeanDefinition beanDefinition) {
        MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
        if (propertyValues == null) {
            propertyValues = new MutablePropertyValues();
            beanDefinition.setPropertyValues(propertyValues);
        }
        return propertyValues;
    }

    protected void removeOldPropertyIfNeeded(@Nonnull BeanAndPropertyName beanAndPropertyName, @Nonnull MutablePropertyValues propertyValues) {
        final PropertyValue propertyValue = propertyValues.getPropertyValue(beanAndPropertyName.getPropertyName());
        if (propertyValue != null) {
            propertyValues.removePropertyValue(propertyValue);
        }
    }

    @Nullable
    protected BeanAndPropertyName getBeanAndPropertyNameOf(@Nonnull String key) throws BeansException {
        final String[] parts = key.split("[\\#|\\@]");
        if (parts.length != 2) {
            throw new FatalBeanException("The property key '" + key + "' is invalid. The syntax is: <beanName>#<propertyName>");
        }
        final PropertyValueType propertyValueType = key.contains("#") ? value : reference;
        return new BeanAndPropertyName(parts[0], parts[1], propertyValueType);
    }
    
    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
    
    protected enum PropertyValueType {
        value,
        reference
    }
    
    protected class BeanAndPropertyName {
        private final String _beanName;
        private final String _propertyName;
        private final PropertyValueType _propertyValueType;

        protected BeanAndPropertyName(@Nonnull String beanName, @Nonnull String propertyName, @Nonnull PropertyValueType propertyValueType) {
            _beanName = beanName;
            _propertyName = propertyName;
            _propertyValueType = propertyValueType;
        }

        @Nonnull
        protected String getBeanName() {
            return _beanName;
        }

        @Nonnull
        protected String getPropertyName() {
            return _propertyName;
        }

        @Nonnull
        public PropertyValueType getPropertyValueType() {
            return _propertyValueType;
        }
    }
}
