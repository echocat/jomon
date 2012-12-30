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
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class EnumLocalizer {

    private final ResourceBundleFactory<?> _factory;

    public EnumLocalizer(@Nonnull ResourceBundleFactory<?> factory) {
        _factory = factory;
    }

    @Nonnull
    public String getMessageFor(@Nonnull Enum<?> value, @Nonnull Locale locale) throws MissingResourceException {
        final ResourceBundle resourceBundle = _factory.getFor(value.getClass(), locale);
        return resourceBundle.getString(value.name());
    }

}
