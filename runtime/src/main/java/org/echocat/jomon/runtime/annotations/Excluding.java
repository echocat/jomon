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

package org.echocat.jomon.runtime.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>Is used to document an parameter, field, ... which was excluded from a range.</p>
 *
 * <p>Example: <code>public boolean isInRange({@link Including @Including} int start, {@link Excluding @Excluding} int end)</code></p>
 */
@Documented
@Retention(RUNTIME)
@Target({PARAMETER, LOCAL_VARIABLE, FIELD, METHOD, ANNOTATION_TYPE, TYPE})
public @interface Excluding {}
