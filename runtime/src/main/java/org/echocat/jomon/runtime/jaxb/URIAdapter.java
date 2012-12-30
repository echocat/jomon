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
import java.net.URI;

public class URIAdapter extends XmlAdapter<String, URI> {

    @Override
    public URI unmarshal(String v) throws Exception {
        return v != null ? new URI(v) : null;
    }

    @Override
    public String marshal(URI v) throws Exception {
        return v != null ? v.toString() : null;
    }
}
