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

package org.echocat.jomon.runtime.i18n;

import org.echocat.jomon.runtime.system.DynamicClassLoader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

import static java.nio.charset.Charset.forName;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@ThreadSafe
public class RecursiveResourceBundleFactory {
    
    private final Map<Class<?>, Map<Locale, ResourceBundle>> _typeToNotRecursiveBundleCache = new WeakHashMap<>();
    private final Map<ClassLoader, Map<String, Map<Locale, ResourceBundle>>> _classLoaderToPackageNameAndNotRecursiveBundleCache = new WeakHashMap<>();
    
    private Charset _charset = forName("UTF-8");

    @Nonnull
    public Charset getCharset() {
        return _charset;
    }

    public void setCharset(@Nonnull Charset charset) {
        synchronized (this) {
            _charset = charset;
            flushAllEntries();
        }
    }
    
    public void flushEntriesOf(@Nonnull ClassLoader classLoader) {
        synchronized (this) {
            _classLoaderToPackageNameAndNotRecursiveBundleCache.remove(classLoader);
            final Iterator<Class<?>> i = _typeToNotRecursiveBundleCache.keySet().iterator();
            while (i.hasNext()) {
                final Class<?> type = i.next();
                if (classLoader.equals(type.getClassLoader())) {
                    i.remove();
                }
            }
        }
    }
    
    public void flushAllEntries() {
        synchronized (this) {
            _typeToNotRecursiveBundleCache.clear();
            _classLoaderToPackageNameAndNotRecursiveBundleCache.clear();
        }
    }
    
    @Nonnull
    public ResourceBundle getFor(@Nonnull Class<?> type, @Nullable Locale locale) {
        return getFor(type, null, locale);
    }

    @Nonnull
    public ResourceBundle getFor(@Nonnull Class<?> type, @Nullable ClassLoader classLoader, @Nullable Locale locale) {
        final List<ResourceBundle> bundles = new ArrayList<>();
        Class<?> current = type;
        while (type != null && !Object.class.equals(current)) {
            bundles.addAll(getAllRecursivelyWithNoInheritanceFor(current, classLoader, locale));
            current = current.getSuperclass();
        }
        return new CombinedResourceBundle(bundles);
    }

    @Nonnull
    protected List<ResourceBundle> getAllRecursivelyWithNoInheritanceFor(@Nonnull Class<?> type, @Nullable ClassLoader classLoader, @Nullable Locale locale) {
        final ClassLoader targetClassLoader = classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
        final List<ResourceBundle> bundles = new ArrayList<>();
        if (targetClassLoader != null) {
            final ResourceBundle bundleForType = tryFindFor(type, locale, targetClassLoader);
            if (bundleForType != null) {
                bundles.add(bundleForType);
            }
            bundles.addAll(getAllRecursivelyFor(type.getPackage(), locale, targetClassLoader));
        }
        return bundles;
    }

    @Nullable
    protected ResourceBundle tryFindFor(@Nonnull Class<?> type, @Nullable Locale forLocale, @Nonnull ClassLoader withClassLoader) {
        final String fileName = buildMessagePropertiesFileNameFor(type, forLocale);
        final ResourceBundle bundle;
        if (withClassLoader instanceof DynamicClassLoader && ((DynamicClassLoader) withClassLoader).isDynamic()) {
            bundle = loadBundles(fileName, withClassLoader);
        } else {
            synchronized (this) {
                Map<Locale, ResourceBundle> localeToResourceBundleCache = _typeToNotRecursiveBundleCache.get(type);
                if (localeToResourceBundleCache == null) {
                    localeToResourceBundleCache = new HashMap<>();
                    _typeToNotRecursiveBundleCache.put(type, localeToResourceBundleCache);
                }
                if (localeToResourceBundleCache.containsKey(forLocale)) {
                    bundle = localeToResourceBundleCache.get(forLocale);
                } else {
                    bundle = loadBundles(fileName, withClassLoader);
                    localeToResourceBundleCache.put(forLocale, bundle);
                }
            }
        }
        return bundle;
    }

    @Nonnull
    protected List<ResourceBundle> getAllRecursivelyFor(@Nonnull Package aPackage, @Nullable Locale forLocale, @Nonnull ClassLoader withClassLoader) {
        final List<ResourceBundle> bundles = new ArrayList<>();
        String current = aPackage.getName();
        while (current != null) {
            final ResourceBundle bundle = tryFindFor(current, forLocale, withClassLoader);
            if (bundle != null) {
                bundles.add(bundle);
            }
            final int lastDot = current.lastIndexOf('.');
            if (lastDot >= 0) {
                current = current.substring(0, lastDot);
            } else if (!current.isEmpty()) {
                current = "";
            } else {
                current = null;
            }
        }
        return bundles;
    }

    @Nullable
    protected ResourceBundle tryFindFor(@Nonnull String aPackageName, @Nullable Locale forLocale, @Nonnull ClassLoader withClassLoader) {
        final String fileName = buildMessagePropertiesFileNameFor(aPackageName, forLocale);
        final ResourceBundle bundle;
        if (withClassLoader instanceof DynamicClassLoader && ((DynamicClassLoader) withClassLoader).isDynamic()) {
            bundle = loadBundles(fileName, withClassLoader);
        } else {
            synchronized (this) {
                Map<String, Map<Locale, ResourceBundle>> packageNameToBundle = _classLoaderToPackageNameAndNotRecursiveBundleCache.get(withClassLoader);
                if (packageNameToBundle == null) {
                    packageNameToBundle = new HashMap<>();
                    _classLoaderToPackageNameAndNotRecursiveBundleCache.put(withClassLoader, packageNameToBundle);
                }
                Map<Locale, ResourceBundle> localeToResourceBundle = packageNameToBundle.get(aPackageName);
                if (localeToResourceBundle == null) {
                    localeToResourceBundle = new HashMap<>();
                    packageNameToBundle.put(aPackageName, localeToResourceBundle);
                }
                if (localeToResourceBundle.containsKey(forLocale)) {
                    bundle = localeToResourceBundle.get(forLocale);
                } else {
                    bundle = loadBundles(fileName, withClassLoader);
                    localeToResourceBundle.put(forLocale, bundle);
                }
            }
        }
        return bundle;
    }

    @Nullable
    protected ResourceBundle loadBundles(@Nonnull String withName, @Nonnull ClassLoader from) {
        final List<ResourceBundle> bundles = new ArrayList<>();
        final Enumeration<URL> i;
        try {
            i = from.getResources(withName);
        } catch (IOException e) {
            throw new RuntimeException("Could not find all resourceBundles with name '" + withName + "' in " + from + ".", e);
        }
        while (i.hasMoreElements()) {
            final URL propertiesUrl = i.nextElement();
            try (final InputStream is = propertiesUrl.openStream()) {
                try (final Reader reader = new InputStreamReader(is, _charset)) {
                    bundles.add(new PropertyResourceBundle(reader));
                }
            } catch (IOException e) {
                throw new RuntimeException("Could not load resourceBundle from '" + propertiesUrl + "'.", e);
            }
        }
        return bundles.isEmpty() ? null : new CombinedResourceBundle(bundles);
    }

    @Nonnull
    protected String buildMessagePropertiesFileNameFor(@Nonnull Class<?> type, @Nullable Locale locale) {
        return type.getName().replace('.', '/') + buildMessagePropertiesFileNameSuffixFor(locale);
    }

    @Nonnull
    protected String buildMessagePropertiesFileNameFor(@Nonnull String packageName, @Nullable Locale locale) {
        return packageName.replace('.', '/') + (packageName.isEmpty() ? "" : "/") + "messages" + buildMessagePropertiesFileNameSuffixFor(locale);
    }
    
    @Nonnull
    protected String buildMessagePropertiesFileNameSuffixFor(@Nullable Locale locale) {
        final StringBuilder sb = new StringBuilder();
        if (locale != null && !isEmpty(locale.getLanguage())) {
            sb.append('_').append(locale);
        }
        sb.append(".properties");
        return sb.toString();
    }
}
