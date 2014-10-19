/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2014 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.runtime.i18n;

import org.echocat.jomon.runtime.iterators.ConvertingIterator;
import org.echocat.jomon.runtime.util.Entry;
import org.echocat.jomon.runtime.util.Entry.Impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.text.MessageFormat.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@NotThreadSafe
public class ResourceBundles implements Iterable<Entry<Locale, ResourceBundle>>, Localizer {

    private static final Comparator<Locale> COMPARATOR = new Comparator<Locale>() { @Override public int compare(Locale o1, Locale o2) {
        return asString(o1).compareTo(asString(o2));
    }};

    private final Map<Locale, ResourceBundle> _localeToBundle = new TreeMap<>(COMPARATOR);
    private final Map<Locale, ResourceBundle> _localeToBundleCache = new ConcurrentHashMap<>();

    public void putBundle(@Nullable Locale locale, @Nonnull ResourceBundle resourceBundle) {
        _localeToBundle.put(locale, resourceBundle);
    }
    
    @Nonnull
    public ResourceBundle getBundle(@Nullable Locale locale) throws NoSuchElementException {
        ResourceBundle resourceBundle = _localeToBundleCache.get(locale);
        if (resourceBundle == null) {
            final List<ResourceBundle> candidates = new ArrayList<>(10);
            if (locale != null && !isEmpty(locale.getLanguage()) && !isEmpty(locale.getCountry()) && !isEmpty(locale.getVariant())) {
                final ResourceBundle bundle = _localeToBundle.get(new Locale(locale.getLanguage(), locale.getCountry(), locale.getVariant()));
                if (bundle != null) {
                    candidates.add(bundle);
                }
            }
            if (locale != null && !isEmpty(locale.getLanguage()) && !isEmpty(locale.getCountry())) {
                final ResourceBundle bundle = _localeToBundle.get(new Locale(locale.getLanguage(), locale.getCountry()));
                if (bundle != null) {
                    candidates.add(bundle);
                }
            }
            if (locale != null && !isEmpty(locale.getLanguage())) {
                final ResourceBundle bundle = _localeToBundle.get(new Locale(locale.getLanguage()));
                if (bundle != null) {
                    candidates.add(bundle);
                }
            }
            final ResourceBundle bundle = _localeToBundle.get(new Locale(""));
            if (bundle != null) {
                candidates.add(bundle);
            }
            if (candidates.isEmpty()) {
                throw new NoSuchElementException("There is no bundle for locale " + locale + ".");
            }
            resourceBundle = new CombinedResourceBundle(candidates);
            _localeToBundleCache.put(locale, resourceBundle);
        }
        return resourceBundle;
    }
    
    protected boolean containsBundle(@Nullable Locale locale) {
        return _localeToBundle.containsKey(locale);
    }

    /**
     * @deprecated Use {@link #localize(java.util.Locale, String, Object...)} in the future.
     */
    @Deprecated
    @Nonnull
    public String get(@Nullable Locale locale, @Nonnull String key) {
        return localize(locale, key, null);
    }

    /**
     * @deprecated Use {@link #localize(java.util.Locale, String, Object...)} in the future.
     */
    @Deprecated
    @Nonnull
    public String get(@Nullable Locale locale, @Nonnull String key, @Nullable Object... parameters) {
        return localize(locale, key, parameters);
    }

    @Override
    @Nonnull
    public String localize(@Nullable Locale locale, @Nonnull String key, @Nullable Object... parameters) {
        final ResourceBundle bundle = getBundle(locale);
        final String message = bundle.getString(key);
        return parameters != null && parameters.length > 0 ? format(message, parameters) : message;
    }
    
    @Override
    public Iterator<Entry<Locale, ResourceBundle>> iterator() {
        return new ConvertingIterator<Map.Entry<Locale, ResourceBundle>, Entry<Locale, ResourceBundle>>(_localeToBundle.entrySet().iterator()) { @Override protected Entry<Locale, ResourceBundle> convert(Map.Entry<Locale, ResourceBundle> input) {
            return new Impl<>(input.getKey(), input.getValue());
        }};
    }

    @Nonnull
    private static String asString(@Nullable Locale locale) {
        return locale != null ? locale.toString() : "";
    }

}
