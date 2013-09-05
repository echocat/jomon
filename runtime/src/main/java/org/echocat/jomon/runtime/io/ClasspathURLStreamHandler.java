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

package org.echocat.jomon.runtime.io;

import javax.annotation.Nonnull;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class ClasspathURLStreamHandler extends URLStreamHandler {

    @Override
    @Nonnull
    protected URLConnection openConnection(@Nonnull URL u) throws IOException {
        final String path = u.getPath();
        if (path == null) {
            throw new FileNotFoundException(u.toExternalForm());
        }
        final URL resource = getClass().getClassLoader().getResource(path.length() > 1 ? path.substring(1) : path);
        if (resource == null) {
            throw new FileNotFoundException(u.toExternalForm());
        }
        return resource.openConnection();
    }

}
