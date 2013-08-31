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

package org.echocat.jomon.spring;

import org.echocat.jomon.cache.CacheListener;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nonnull;

public class AutomaticCacheListenerDiscovery extends AutomaticCollectionBasedServicesDiscovery<CacheListener> {

    protected AutomaticCacheListenerDiscovery() {
        super(CacheListener.class);
    }

    @Override
    protected boolean isApplicable(@Nonnull Class<?> type, @Nonnull String withBeanName, @Nonnull ApplicationContext of) {
        return CacheListener.class.isAssignableFrom(type);
    }

}
