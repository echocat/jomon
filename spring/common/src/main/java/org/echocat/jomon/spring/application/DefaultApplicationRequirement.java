/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.spring.application;

import javax.annotation.Nonnull;
import java.io.File;

public class DefaultApplicationRequirement extends ApplicationRequirementSupport<DefaultApplicationRequirement> {

    @Nonnull
    public static DefaultApplicationRequirement applicationFor(@Nonnull ApplicationContextRequirement applicationContextRequirement) {
        return new DefaultApplicationRequirement(applicationContextRequirement);
    }

    @Nonnull
    public static DefaultApplicationRequirement xmlConfiguredApplicationFor(@Nonnull Class<?> type, @Nonnull String fileName) {
        return applicationFor(XmlBasedApplicationContextRequirement.applicationContextFor(type, fileName));
    }

    @Nonnull
    public static DefaultApplicationRequirement xmlConfiguredApplicationFor(@Nonnull ClassLoader loader, @Nonnull String fileName) {
        return applicationFor(XmlBasedApplicationContextRequirement.applicationContextFor(loader, fileName));
    }

    @Nonnull
    public static DefaultApplicationRequirement xmlConfiguredApplicationFor(@Nonnull File file) {
        return applicationFor(XmlBasedApplicationContextRequirement.applicationContextFor(file));
    }

    @Nonnull
    public static DefaultApplicationRequirement javaConfiguredApplicationFor(@Nonnull Class<?>... types) {
        return applicationFor(JavaBasedApplicationContextRequirement.applicationContextFor(types));
    }

    public DefaultApplicationRequirement(@Nonnull ApplicationContextRequirement applicationContextRequirement) {
        super(applicationContextRequirement);
    }

}
