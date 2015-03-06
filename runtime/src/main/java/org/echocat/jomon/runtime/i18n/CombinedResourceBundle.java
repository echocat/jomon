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

import javax.annotation.Nonnull;
import java.util.*;

import static com.google.common.collect.Iterators.asEnumeration;
import static java.util.Collections.unmodifiableList;

public class CombinedResourceBundle extends ResourceBundle {
    
    private final List<ResourceBundle> _resourceBundles;

    public CombinedResourceBundle(@Nonnull List<ResourceBundle> resourceBundles) {
        _resourceBundles = unmodifiableList(resourceBundles);
    }

    @Override
    protected Object handleGetObject(String key) {
        Object result = null;
        final Iterator<ResourceBundle> i = _resourceBundles.iterator();
        while (result == null && i.hasNext()) {
            final ResourceBundle resourceBundle = i.next();
            if (resourceBundle.containsKey(key)) {
                result = resourceBundle.getObject(key);
            }
        }
        return result;
    }

    @Override
    public Enumeration<String> getKeys() {
        final Set<String> keys = new HashSet<>();
        for (final ResourceBundle resourceBundle : _resourceBundles) {
            keys.addAll(resourceBundle.keySet());
        }
        return asEnumeration(keys.iterator());
    }

    @Override
    public Locale getLocale() {

        return !_resourceBundles.isEmpty() ? _resourceBundles.iterator().next().getLocale() : null;
    }

    @Nonnull
    public List<ResourceBundle> getResourceBundles() {
        return _resourceBundles;
    }
}
