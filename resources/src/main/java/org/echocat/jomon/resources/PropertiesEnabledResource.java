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

package org.echocat.jomon.resources;

import org.echocat.jomon.runtime.util.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

public interface PropertiesEnabledResource<PV> extends Resource {

    @Nonnull
    public Iterable<Entry<String, PV>> getProperties() throws IOException;

    @Nullable
    public PV getProperty(@Nonnull String name) throws IOException;

    public void setProperty(@Nonnull String name, @Nullable Object value) throws IOException;

    public void removeProperty(@Nonnull String name) throws IOException;

    @Nonnull
    public Class<PV> getPropertyValueType();

}
