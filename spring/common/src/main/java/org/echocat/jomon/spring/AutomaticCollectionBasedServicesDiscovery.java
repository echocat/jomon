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

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.LinkedHashSet;

public abstract class AutomaticCollectionBasedServicesDiscovery<V> extends AutomaticServicesDiscovery<V, Collection<V>> {

    protected AutomaticCollectionBasedServicesDiscovery(@Nonnull Class<V> expectedType) {
        super(expectedType);
    }

    @Override
    protected Collection<V> createNewCollection() {
        return new LinkedHashSet<>();
    }
}
