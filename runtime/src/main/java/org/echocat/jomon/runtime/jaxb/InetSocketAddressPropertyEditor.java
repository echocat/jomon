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

package org.echocat.jomon.runtime.jaxb;

import java.beans.PropertyEditorSupport;
import java.net.InetSocketAddress;

public class InetSocketAddressPropertyEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        final InetSocketAddress address;
        if (text != null) {
            final String trimmedText = text.trim();
            if (!trimmedText.isEmpty()) {
                final int lastDoubleDot = trimmedText.lastIndexOf(':');
                if (lastDoubleDot > 0 && lastDoubleDot + 1 < trimmedText.length()) {
                    final String host = trimmedText.substring(0, lastDoubleDot).trim();
                    final String plainPort = trimmedText.substring(lastDoubleDot + 1).trim();
                    final int port;
                    try {
                        port = Integer.parseInt(plainPort);
                    } catch (NumberFormatException ignored) {
                        throw new IllegalArgumentException("Illegal port: " + plainPort);
                    }
                    address = new InetSocketAddress(host, port);
                } else {
                    throw new IllegalArgumentException("Port missing");
                }
            } else {
                address = null;
            }
        } else {
            address = null;
        }
        setValue(address);
    }

}
