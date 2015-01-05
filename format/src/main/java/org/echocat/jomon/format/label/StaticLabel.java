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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ThreadSafe
@Immutable
public class StaticLabel extends LabelSupport {

    @Nonnull
    private final Map<Locale, MessageFormat> _localeToFormat = new ConcurrentHashMap<>();
    @Nonnull
    private final String _content;

    public StaticLabel(@Nonnull String content) {
        _content = content;
    }

    @Nonnull
    @Override
    public String toLocalized(@Nonnull Locale locale, @Nullable Object... arguments) {
        final String result;
        if (arguments != null && arguments.length > 0) {
            result = messageFormatFor(locale).format(arguments);
        } else {
            result = _content;
        }
        return result;
    }

    @Nonnull
    protected MessageFormat messageFormatFor(@Nonnull Locale locale) {
        MessageFormat format = _localeToFormat.get(locale);
        if (format == null) {
            format = new MessageFormat(_content, locale);
            _localeToFormat.put(locale, format);
        }
        return format;
    }

}
