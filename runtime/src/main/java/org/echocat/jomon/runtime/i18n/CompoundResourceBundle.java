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

import org.echocat.jomon.runtime.util.Entry;

import javax.annotation.Nonnull;
import java.util.*;

import static com.google.common.collect.Iterators.asEnumeration;

public class CompoundResourceBundle extends ResourceBundle {

    private final ResourceBundles _resourceBundles;
    private final Locale _locale;

    public CompoundResourceBundle(@Nonnull ResourceBundles resourceBundles, @Nonnull Locale locale) {
        _resourceBundles = resourceBundles;
        _locale = locale;
    }

    @Override
    protected Object handleGetObject(String key) {
        if (key == null) {
            throw new NullPointerException("This implementation of " + ResourceBundle.class.getName() + " could not handle null keys.");
        }
        return _resourceBundles.localize(_locale, key);
    }

    @Override
    public Enumeration<String> getKeys() {
        final Set<String> keys = new HashSet<>();
        for (final Entry<Locale, ResourceBundle> localeAndBundle : _resourceBundles) {
            final ResourceBundle bundle = localeAndBundle.getValue();
            keys.addAll(bundle.keySet());
        }
        return asEnumeration(keys.iterator());
    }

    @Override
    public Locale getLocale() {
        return _locale;
    }
}
