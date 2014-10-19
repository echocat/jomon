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

package org.echocat.jomon.spring.types;

import org.echocat.jomon.runtime.jaxb.InetSocketAddressPropertyEditor;
import org.echocat.jomon.runtime.logging.LogLevel;
import org.echocat.jomon.runtime.logging.LogLevelPropertyEditor;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;

public class BasePropertyEditorRegistrar implements PropertyEditorRegistrar {

    @Override
    public void registerCustomEditors(@Nonnull PropertyEditorRegistry registry) {
        registry.registerCustomEditor(InetSocketAddress.class, new InetSocketAddressPropertyEditor());
        registry.registerCustomEditor(LogLevel.class, new LogLevelPropertyEditor());
    }

}
