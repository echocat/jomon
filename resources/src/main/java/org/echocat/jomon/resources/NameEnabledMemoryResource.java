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

import javax.annotation.Nonnull;

public class NameEnabledMemoryResource extends MemoryResource implements NameEnabledResource {

    private final String _name;

    public NameEnabledMemoryResource(@Nonnull byte[] content, @Nonnull byte[] md5, @Nonnull ResourceType type, @Nonnull String name, boolean generated) {
        super(content, md5, type, generated);
        _name = name;
    }

    @Nonnull
    @Override
    public String getName() {
        return _name;
    }
}
