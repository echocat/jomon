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

package org.echocat.jomon.cache;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Collection;

public interface LocalTrackingEnabledCacheListener extends CacheListener {

    public void setNumberOfLocalTrackedEvents(int numberOfLocalTrackedEvents);

    public int getNumberOfLocalTrackedEvents();

    @Nonnull
    public Collection<? extends Report> getReports();

    public interface Report {

        @Nonnull
        public Event getEvent();

        @Nonnegative
        public long getNumberOfInvocations();

        @Nonnegative
        public double getNumberOfInvocationsPerSecond();

        @Nonnull
        public StackTraceElement[] getStackTrace();

    }

    public interface Event {}
}
