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

package org.echocat.jomon.net;

import org.echocat.jomon.runtime.StringUtils;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlUtils {

    private static final Pattern NORMALIZE_URL_PATTERN = Pattern.compile("[^/]+/\\.\\./");

    @Nonnull
    public static String makeUrlAbsolute(@Nonnull String baseUrl, @Nonnull String url) {
        int i = url.indexOf(':');
        int j = url.indexOf('?');
        final boolean hasProtocol = (i > 0 && (j == -1 || i < j));
        String absoluteUrl;
        if (hasProtocol) {
            absoluteUrl = url;
        } else if (url.startsWith("/")) {
            // Prepend protocol, host and port ...
            i = baseUrl.indexOf("://");
            i = baseUrl.indexOf("/", i + 3);
            absoluteUrl = baseUrl.substring(0, i) + url;
        } else {
            i = baseUrl.indexOf('?');
            if (i == -1) {
                i = baseUrl.length();
            }
            j = baseUrl.indexOf('#');
            if (j == -1) {
                j = baseUrl.length();
            }
            i = Math.min(i, j);
            i = baseUrl.lastIndexOf('/', i);
            absoluteUrl = baseUrl.substring(0, i) + '/' + url;
            // Normalize ...
            Matcher m = NORMALIZE_URL_PATTERN.matcher(absoluteUrl);
            while (m.find()) {
                absoluteUrl = m.replaceFirst("");
                m = NORMALIZE_URL_PATTERN.matcher(absoluteUrl);
            }
        }
        return absoluteUrl;
    }

    @Nonnull
    public static String makeRelative(@Nonnull String url) {
        String result = url;
        if (StringUtils.isNotBlank(url)) {
            final String withoutScheme = removeScheme(url);
            final String[] splits = withoutScheme.split("/", 2);
            if (splits.length > 1) {
                result = "/" + splits[1];
            } else {
                result = "/";
            }
        }
        return result;
    }

    @Nonnull
    public static String removeScheme(@Nonnull String url) {
        String result = url;
        if (StringUtils.isNotBlank(url) && url.contains("://")) {
            final String[] splits = url.split("://", 2);
            if (splits.length > 1) {
                result = splits[1];
            }
        }
        return result;
    }

    private UrlUtils() {}

}
