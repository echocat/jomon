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

package org.echocat.jomon.runtime;

import org.slf4j.spi.MDCAdapter;

import javax.annotation.Nonnull;
import java.util.Map;

public class FixingSlf4jMDCAdapter implements MDCAdapter {

    private final MDCAdapter _delegate;

    public FixingSlf4jMDCAdapter(@Nonnull MDCAdapter delegate) {
        _delegate = delegate;
    }

    @Override
    public void put(String key, String val) {
        _delegate.put(key, val != null ? val : "");
    }

    @Override
    public String get(String key) {
        return _delegate.get(key);
    }

    @Override
    public void remove(String key) {
        _delegate.remove(key);
    }

    @Override
    public void clear() {
        _delegate.clear();
    }

    @Override
    public Map<?, ?> getCopyOfContextMap() {
        return _delegate.getCopyOfContextMap();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setContextMap(Map contextMap) {
        _delegate.setContextMap(contextMap);
    }
}
