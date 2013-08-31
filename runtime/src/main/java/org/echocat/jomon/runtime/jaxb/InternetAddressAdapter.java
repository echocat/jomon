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

import javax.mail.internet.InternetAddress;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class InternetAddressAdapter extends XmlAdapter<String, InternetAddress> {

    @Override
    public InternetAddress unmarshal(String addressAsString) throws Exception {
        final InternetAddress[] addresses = addressAsString != null ? InternetAddress.parse(addressAsString) : new InternetAddress[0];
        final InternetAddress result;
        if (addresses.length == 1) {
            result = addresses[0];
        } else if (addresses.length == 0) {
            result = null;
        } else {
            throw new IllegalArgumentException("There was multiple addresses provided: " + addressAsString);
        }
        return result;
    }

    @Override
    public String marshal(InternetAddress address) throws Exception {
        return address != null ? address.toString() : null;
    }
}
