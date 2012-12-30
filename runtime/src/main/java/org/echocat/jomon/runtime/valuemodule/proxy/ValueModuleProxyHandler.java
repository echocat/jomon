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

package org.echocat.jomon.runtime.valuemodule.proxy;

import javassist.util.proxy.MethodHandler;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

public interface ValueModuleProxyHandler extends MethodHandler {

    @Override
    public Object invoke(@Nonnull Object proxy, @Nonnull Method thisMethod, @Nonnull Method proceed, @Nonnull Object[] args) throws Throwable;
}
