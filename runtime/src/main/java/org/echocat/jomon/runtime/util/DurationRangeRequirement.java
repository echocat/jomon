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

package org.echocat.jomon.runtime.util;

import org.echocat.jomon.runtime.annotations.Excluding;
import org.echocat.jomon.runtime.annotations.Including;
import org.echocat.jomon.runtime.generation.RangeRequirementSupport;
import org.echocat.jomon.runtime.util.DurationRangeRequirement.Adapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@ThreadSafe
@Immutable
@XmlJavaTypeAdapter(Adapter.class)
public class DurationRangeRequirement extends RangeRequirementSupport<Duration, DurationRange> implements DurationRequirement {

    public DurationRangeRequirement(@Nullable @Including String from, @Nullable @Excluding String to) {
        this(from != null ? new Duration(from) : null, to != null ? new Duration(to) : null);
    }

    public DurationRangeRequirement(@Nullable @Including Long from, @Nullable @Excluding Long to) {
        this(from != null ? new Duration(from) : null, to != null ? new Duration(to) : null);
    }

    public DurationRangeRequirement(@Nullable @Including Duration from, @Nullable @Excluding Duration to) {
        this(new DurationRange(from, to));
    }

    public DurationRangeRequirement(@Nonnull DurationRange range) {
        super(range);
    }

    @XmlRootElement(name = "durationRangeRequirement")
    @XmlType(name = "durationRangeRequirement")
    public static class Container extends RangeRequirementSupport.Container<Duration> {}


    public static class Adapter extends RangeRequirementSupport.Adapter<Duration, Container, DurationRange> {

        @Nonnull
        @Override
        protected Container newContainer() {
            return new Container();
        }

        @Nonnull
        @Override
        protected DurationRange newRangeRequirement(@Nullable @Including Duration from, @Nullable @Excluding Duration to) {
            return new DurationRange(from, to);
        }
    }

}
