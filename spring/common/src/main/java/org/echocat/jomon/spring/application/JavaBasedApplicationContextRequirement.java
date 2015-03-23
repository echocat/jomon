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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.echocat.jomon.runtime.CollectionUtils.addAll;
import static org.echocat.jomon.runtime.CollectionUtils.asImmutableList;

public class JavaBasedApplicationContextRequirement extends ApplicationContextRequirement.Support<JavaBasedApplicationContextRequirement> {

    @Nonnull
    public static JavaBasedApplicationContextRequirement applicationContextFor(@Nonnull Iterable<Class<?>> classes) {
        return applicationContext().withClasses(classes);
    }

    @Nonnull
    public static JavaBasedApplicationContextRequirement applicationContextFor(@Nonnull Class<?>... classes) {
        return applicationContext().withClasses(classes);
    }

    @Nonnull
    public static JavaBasedApplicationContextRequirement applicationContext() {
        return new JavaBasedApplicationContextRequirement();
    }

    @Nonnull
    private final List<Class<?>> _classes = new ArrayList<>();

    @Nonnull
    public JavaBasedApplicationContextRequirement withClasses(@Nonnull Iterable<Class<?>> classes) {
        addAll(_classes, classes);
        return this;
    }

    @Nonnull
    public JavaBasedApplicationContextRequirement withClasses(@Nonnull Class<?>... classes) {
        addAll(_classes, classes);
        return this;
    }

    @Nonnull
    public JavaBasedApplicationContextRequirement withClass(@Nonnull Class<?> aClass) {
        return withClasses(aClass);
    }

    @Nonnull
    public Collection<Class<?>> getClasses() {
        return asImmutableList(_classes);
    }


}
