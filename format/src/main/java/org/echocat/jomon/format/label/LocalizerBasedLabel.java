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

package org.echocat.jomon.format.label;

import org.echocat.jomon.runtime.i18n.Localizer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

public class LocalizerBasedLabel extends LabelSupport implements MessageKeyEnabledLabel {

    @Nonnull
    private final Localizer _localizer;
    @Nonnull
    private final String _messageKey;

    public LocalizerBasedLabel(@Nonnull Localizer localizer, @Nonnull String messageKey) {
        _localizer = localizer;
        _messageKey = messageKey;
    }

    @Nonnull
    @Override
    public String toLocalized(@Nonnull Locale locale, @Nullable Object... arguments) {
        return _localizer.localize(locale, _messageKey, arguments);
    }

    @Override
    @Nonnull
    public String getMessageKey() {
        return _messageKey;
    }

    @Override
    public String toString() {
        return getMessageKey() + ":" + super.toString();
    }
}
