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

package org.echocat.jomon.runtime.valuemodule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collection;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public interface ValueModule {

    @Nonnull
    public String getId();

    @Nullable
    public Collection<? extends ValueModule> getIncludes();

    @Retention(RUNTIME)
    @Target({TYPE})
    public @interface IsModularizedBy {
        public Class<? extends ValueModule> value();
    }

    @Retention(RUNTIME)
    @Target({TYPE})
    public @interface IsModuleAnnotation {
        public Class<? extends Annotation> value();
    }

}
