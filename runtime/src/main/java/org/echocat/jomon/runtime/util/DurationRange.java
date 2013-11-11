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
import org.echocat.jomon.runtime.util.DurationRange.Adapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@ThreadSafe
@Immutable
@XmlJavaTypeAdapter(Adapter.class)
public class DurationRange extends RangeSupport<Duration> {

    public DurationRange(@Nullable @Including String from, @Nullable @Excluding String to) {
        super(from != null ? new Duration(from) : null, to != null ? new Duration(to) : null);
    }

    public DurationRange(@Nullable @Including Long from, @Nullable @Excluding Long to) {
        super(from != null ? new Duration(from) : null, to != null ? new Duration(to) : null);
    }

    public DurationRange(@Nullable @Including Duration from, @Nullable @Excluding Duration to) {
        super(from, to);
    }

    @Override
    public boolean apply(@Nullable Duration toTest) {
        return toTest != null && matchesFrom(toTest) && matchesTo(toTest);
    }

    protected boolean matchesFrom(@Nonnull Duration toTest) {
        final Duration from = getFrom();
        return from == null || from.isGreaterThanOrEqualTo(toTest) || from.equals(toTest);
    }

    protected boolean  matchesTo(@Nonnull Duration toTest) {
        final Duration to = getTo();
        return to == null || to.isLessThan(toTest);
    }

    @XmlRootElement(name = "durationRange")
    @XmlType(name = "durationRangeType")
    public static class Container {

        private Long _from;
        private Long _to;

        @XmlAttribute(name = "from")
        public Long getFrom() {
            return _from;
        }

        public void setFrom(Long from) {
            _from = from;
        }

        @XmlAttribute(name = "to")
        public Long getTo() {
            return _to;
        }

        public void setTo(Long to) {
            _to = to;
        }
    }

    public static class Adapter extends XmlAdapter<Container, DurationRange> {

        @Override
        public DurationRange unmarshal(Container v) throws Exception {
            final DurationRange result;
            if (v != null) {
                final Long from = v.getFrom();
                final Long to = v.getTo();
                result = new DurationRange(from != null ? new Duration(from) : null, to != null ? new Duration(to) : null);
            } else {
                result = null;
            }
            return result;
        }

        @Override
        public Container marshal(DurationRange v) throws Exception {
            final Container result;
            if (v != null) {
                result = new Container();
                final Duration from = v.getFrom();
                final Duration to = v.getTo();
                result.setFrom(from != null ? from.toMilliSeconds() : null);
                result.setTo(to != null ? to.toMilliSeconds() : null);
            } else {
                result = null;
            }
            return result;
        }
    }

}
