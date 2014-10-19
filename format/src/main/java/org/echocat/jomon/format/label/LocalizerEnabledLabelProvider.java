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

import static org.echocat.jomon.format.label.Labels.labelFor;

public class LocalizerEnabledLabelProvider implements MessageKeyEnabledLabelProvider {

    @Nonnull
    public static LocalizerEnabledLabelProvider labelProviderFor(@Nonnull Localizer localizer) {
        return new LocalizerEnabledLabelProvider(localizer);
    }

    @Nonnull
    private final Localizer _localizer;

    public LocalizerEnabledLabelProvider(@Nonnull Localizer localizer) {
        _localizer = localizer;
    }

    @Nonnull
    @Override
    public Label provide(@Nonnull String messageKey) {
        return labelFor(_localizer, messageKey);
    }

}
