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

package org.echocat.jomon.net.http;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class HttpResponseException extends IOException {
    
    private final int _statusCode;
    private final String _statusMessage;
    private final String _body;
    
    public HttpResponseException(@Nonnegative int statusCode, @Nullable String statusMessage, @Nullable String body) {
        super(buildMessageFrom(statusCode, statusMessage, body));
        _statusCode = statusCode;
        _statusMessage = statusMessage;
        _body = body;
    }

    @Nonnull
    private static String buildMessageFrom(@Nonnegative int statusCode, @Nullable String statusMessage, @Nullable String body) {
        final StringBuilder sb = new StringBuilder();
        sb.append(statusCode);
        if (!isEmpty(statusMessage)) {
            sb.append(": ").append(statusMessage);
        }
        if (!isEmpty(body)) {
            sb.append("\n===============================================================================================\n").append(body);
        }
        return sb.toString();
    }

    @Nonnegative
    public int getStatusCode() {
        return _statusCode;
    }

    @Nullable
    public String getStatusMessage() {
        return _statusMessage;
    }

    @Nullable
    public String getBody() {
        return _body;
    }
}
