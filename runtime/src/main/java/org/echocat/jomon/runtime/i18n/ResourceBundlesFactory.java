/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.runtime.i18n;

import javax.annotation.Nonnull;
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
        synchronized (this) {
            ResourceBundles resourceBundles = _typeToBundleCache.get(type);
            if (resourceBundles == null) {
                resourceBundles = new ResourceBundles();
                for (Locale locale : _locales) {
                    final ResourceBundle resourceBundle = _recursiveResourceBundleFactory.getFor(type, locale);
                    resourceBundles.putBundle(locale, resourceBundle);
                }
                _typeToBundleCache.put(type, resourceBundles);
            }
            return resourceBundles;
        }
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
