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

import javax.annotation.Nonnull;

public class OptimizationContextUtils {

    @Nonnull
    public static Feature feature(@Nonnull Class<?> owner, @Nonnull String name) {
        final String featureName = owner.getName() + "." + name;
        return new Feature() {
            @Override
            public boolean equals(Object o) {
                final boolean result;
                if (this == o) {
                    result = true;
                } else if (o == null || !Feature.class.isInstance(o)) {
                    result = false;
                } else {
                    final Feature that = (Feature) o;
                    result = toString().equals(that.toString());
                }
                return result;
            }

            @Override
            public int hashCode() {
                return featureName.hashCode();
            }

            @Override
            public String toString() {
                return featureName;
            }
        };
    }

    private OptimizationContextUtils() {}



}
