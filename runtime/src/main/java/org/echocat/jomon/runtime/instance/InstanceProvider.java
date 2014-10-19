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

package org.echocat.jomon.runtime.instance;

import javax.annotation.Nonnull;

public interface InstanceProvider {

    public enum Type {
        singleton,
        multiton,
        undefined
    }

    @Nonnull
    public <T> T provideFor(@Nonnull String name, @Nonnull Class<T> clazz, @Nonnull Type type);

    @Nonnull
    public <T> T provideFor(@Nonnull Class<T> clazz, @Nonnull Type type);

    @Nonnull
    public <T> T provideFor(@Nonnull String name, @Nonnull Class<T> clazz);

    @Nonnull
    public <T> T provideFor(@Nonnull Class<T> clazz);

}
