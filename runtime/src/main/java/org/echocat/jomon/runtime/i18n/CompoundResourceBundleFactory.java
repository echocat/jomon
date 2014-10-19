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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.*;

@ThreadSafe
public class CompoundResourceBundleFactory implements ResourceBundleFactory<CompoundResourceBundle> {

    private final ResourceBundlesFactory _resourceBundlesFactory;

    public void flushEntriesOf(@Nonnull ClassLoader classLoader) {
        _resourceBundlesFactory.flushEntriesOf(classLoader);
    }

    public void flushAllEntries() {
        _resourceBundlesFactory.flushAllEntries();
    }

    public CompoundResourceBundleFactory(@Nonnull ResourceBundlesFactory resourceBundlesFactory) {
        _resourceBundlesFactory = resourceBundlesFactory;
    }

    @Override
    @Nonnull
    public CompoundResourceBundle getFor(@Nonnull Class<?> type, @Nonnull Locale locale) {
        final ResourceBundles resourceBundles = _resourceBundlesFactory.getFor(type);
        return new CompoundResourceBundle(resourceBundles, locale);
    }

}
