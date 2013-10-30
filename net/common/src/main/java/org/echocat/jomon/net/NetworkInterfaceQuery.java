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

package org.echocat.jomon.net;

import com.google.common.base.Predicate;
import org.echocat.jomon.runtime.repository.Query;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.NetworkInterface;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static java.util.regex.Pattern.quote;
import static org.echocat.jomon.net.NetworkInterfaceType.typeOf;

public class NetworkInterfaceQuery implements Query, Predicate<NetworkInterface> {

    @Nonnull
    public static NetworkInterfaceQuery networkInterface() {
        return new NetworkInterfaceQuery();
    }

    @Nullable
    private NetworkInterfaceType _type;
    @Nullable
    private Pattern _namePattern;

    @Nonnull
    public NetworkInterfaceQuery whichIsOfType(@Nullable NetworkInterfaceType type) {
        _type = type;
        return this;
    }

    @Nonnull
    public NetworkInterfaceQuery withNameMatches(@Nullable Pattern pattern) {
        _namePattern = pattern;
        return this;
    }

    @Nonnull
    public NetworkInterfaceQuery withName(@Nullable String name) {
        return withNameMatches(name != null ? compile(quote(name)) : null);
    }

    @Nullable
    public NetworkInterfaceType getType() {
        return _type;
    }

    @Nullable
    public Pattern getNamePattern() {
        return _namePattern;
    }

    @Override
    public boolean apply(@Nullable NetworkInterface input) {
        return input != null
            && applyType(input)
            && applyName(input)
            ;
    }

    protected boolean applyType(@Nonnull NetworkInterface of) {
        return _type == null || _type == typeOf(of);
    }

    protected boolean applyName(@Nonnull NetworkInterface of) {
        return _namePattern == null || _namePattern.matcher(of.getName()).matches();
    }

}
