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

import javax.annotation.Nonnull;

@SuppressWarnings("ConstantNamingConvention")
public interface Protocol {

    public static final Protocol tcp = new Protocol() { @Nonnull @Override public String getName() {
        return "tcp";
    }};
    public static final Protocol udp = new Protocol() { @Nonnull @Override public String getName() {
        return "udp";
    }};

    @Nonnull
    public String getName();

}
