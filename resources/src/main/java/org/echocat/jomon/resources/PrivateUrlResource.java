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

package org.echocat.jomon.resources;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URL;

public class PrivateUrlResource extends UrlResourceSupport implements PrivateUrlEnabledResource {

    public PrivateUrlResource(@Nonnull URL url, @Nonnull ResourceType resourceType) {
        this(url, resourceType, false);
    }

    public PrivateUrlResource(@Nonnull URL url, @Nonnull ResourceType resourceType, boolean generated) {
        super(url, resourceType, generated);
    }

    @Nonnull
    @Override
    public URL getPrivateUrl() throws IOException {
        return getUrl();
    }

}
