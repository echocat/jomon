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

package org.echocat.jomon.runtime.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Locale;

import static org.apache.commons.lang3.StringUtils.split;

public class LocaleAdapter extends XmlAdapter<String, Locale> {

    @Override
    public Locale unmarshal(String v) throws Exception {
        final Locale result;
        if (v != null) {
            final String[] localeParts = split(v, '_');
            if (localeParts.length == 3) {
                result = new Locale(localeParts[0], localeParts[1], localeParts[2]);
            } else if (localeParts.length == 2) {
                result = new Locale(localeParts[0], localeParts[1]);
            } else if (localeParts.length == 1) {
                result = new Locale(localeParts[0]);
            } else {
                throw new IllegalArgumentException("There are too many locale parts provided: " + v);
            }
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public String marshal(Locale v) throws Exception {
        return v != null ? v.toString() : null;
    }
}
