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
import javax.annotation.Nullable;
import java.text.MessageFormat;
import java.util.ResourceBundle;

public class ResourceBundleUtils {

    @Nonnull
    public static String formatMessage(@Nonnull ResourceBundle bundle, @Nonnull String messageKey, @Nullable Object... args) {
        final String plainMessage = bundle.getString(messageKey);
        final String message = new MessageFormat(plainMessage, bundle.getLocale()).format(args);
        return message;
    }

    private ResourceBundleUtils() {}
}
