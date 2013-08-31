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

package org.echocat.jomon.resources.optimizing;

import org.echocat.jomon.resources.optimizing.OptimizationContext.Feature;

import static org.echocat.jomon.resources.optimizing.OptimizationContextUtils.feature;

public interface JavaScriptResourceOptimizer extends ResourceOptimizer {

    public static final Feature SKIP_JAVA_SCRIPT_OPTIMIZATION = feature(JavaScriptResourceOptimizer.class, "skipJavaScriptOptimization");

}
