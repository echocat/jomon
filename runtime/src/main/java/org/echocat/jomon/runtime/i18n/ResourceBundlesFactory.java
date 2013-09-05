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
import java.util.*;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Locale.forLanguageTag;

@ThreadSafe
public class ResourceBundlesFactory {

    private final Map<Class<?>, ResourceBundles> _typeToBundleCache = new WeakHashMap<>();
    private final RecursiveResourceBundleFactory _recursiveResourceBundleFactory;

    private Set<Locale> _locales = newHashSet(new Locale(""));

    public ResourceBundlesFactory(@Nonnull RecursiveResourceBundleFactory recursiveResourceBundleFactory) {
        _recursiveResourceBundleFactory = recursiveResourceBundleFactory;
    }

    public void flushEntriesOf(@Nonnull ClassLoader classLoader) {
        _recursiveResourceBundleFactory.flushEntriesOf(classLoader);
    }

    public void flushAllEntries() {
        _recursiveResourceBundleFactory.flushAllEntries();
    }

    @Nonnull
    public ResourceBundles getFor(@Nonnull Class<?> type) {
        return getFor(type, null);
    }

    @Nonnull
    public ResourceBundles getFor(@Nonnull Class<?> type, @Nullable ClassLoader classLoader) {
        final ClassLoader targetClassLoader = classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
        ResourceBundles resourceBundles;
        if (targetClassLoader instanceof DynamicClassLoader && ((DynamicClassLoader) targetClassLoader).isDynamic()) {
            resourceBundles = loadResourceBundles(type, targetClassLoader);
        } else {
            synchronized (this) {
                resourceBundles = _typeToBundleCache.get(type);
                if (resourceBundles == null) {
                    resourceBundles = loadResourceBundles(type, targetClassLoader);
                    _typeToBundleCache.put(type, resourceBundles);
                }
            }
        }
        return resourceBundles;
    }

    @Nonnull
    protected ResourceBundles loadResourceBundles(@Nonnull Class<?> type, @Nonnull ClassLoader classLoader) {
        final ResourceBundles resourceBundles;
        resourceBundles = new ResourceBundles();
        for (Locale locale : _locales) {
            final ResourceBundle resourceBundle = _recursiveResourceBundleFactory.getFor(type, classLoader, locale);
            resourceBundles.putBundle(locale, resourceBundle);
        }
        return resourceBundles;
    }

    @Nonnull
    public Set<Locale> getLocales() {
        return _locales;
    }

    public void setLocalesFromCommaSeparatedString(String localesAsCommaSeparatedString) {
        if (localesAsCommaSeparatedString != null) {
            final Set<Locale> locales = new HashSet<>();
            for (String localeAsString : localesAsCommaSeparatedString.split(",")) {
                final String trimmedLocalAsString = localeAsString.trim();
                if (!trimmedLocalAsString.isEmpty()) {
                    locales.add(forLanguageTag(trimmedLocalAsString));
                }
            }
        } else {
            setLocales(null);
        }
    }
    
    public void setLocales(Set<Locale> locales) {
        synchronized (this) {
            _locales = locales;
            _typeToBundleCache.clear();
        }
    }
}
