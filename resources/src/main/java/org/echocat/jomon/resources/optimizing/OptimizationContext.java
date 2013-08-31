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

import org.echocat.jomon.resources.Resource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;
import java.util.Set;

@ThreadSafe
public interface OptimizationContext {

    public interface Feature {}

    public boolean isFeatureEnabled(@Nonnull Feature feature);

    public void setFeature(@Nonnull Feature feature, boolean enabled);

    @Nonnull
    public Set<Feature> getFeatures();

    @Nonnull
    public Resource optimize(@Nonnull Resource input) throws Exception;

    @Nonnull
    public Collection<Resource> optimize(@Nonnull Collection<Resource> inputs) throws Exception;

    @Nullable
    public String findOptimizeAndReturnUriFor(@Nonnull String uri) throws Exception;

    @Nullable
    public Resource findAndOptimizeFor(@Nonnull String uri) throws Exception;

}
