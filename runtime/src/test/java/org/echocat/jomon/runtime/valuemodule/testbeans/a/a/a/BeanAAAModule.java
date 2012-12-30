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

package org.echocat.jomon.runtime.valuemodule.testbeans.a.a.a;

import org.echocat.jomon.runtime.valuemodule.ValueModule;
import org.echocat.jomon.runtime.valuemodule.ValueModule.IsModuleAnnotation;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.a.a.BeanAAAModule.IsModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Collections.unmodifiableSet;

@IsModuleAnnotation(IsModule.class)
public enum BeanAAAModule implements ValueModule {
    a,
    b,

    all(a, b);

    private final Set<BeanAAAModule> _includes;

    private BeanAAAModule(@Nullable BeanAAAModule... includes) {
        _includes = includes != null && includes.length > 0 ? unmodifiableSet(newHashSet(includes)) : null;
    }

    @Nonnull
    @Override
    public String getId() {
        return name();
    }

    @Nullable
    @Override
    public Collection<BeanAAAModule> getIncludes() {
        return _includes;
    }

    @Retention(RUNTIME)
    @Target({METHOD})
    public @interface IsModule {
        public BeanAAAModule value();
    }

}
