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

package org.echocat.jomon.runtime.date;

import org.echocat.jomon.runtime.annotations.Excluding;
import org.echocat.jomon.runtime.annotations.Including;
import org.echocat.jomon.runtime.date.DateRange.Adapter;
import org.echocat.jomon.runtime.util.Duration;
import org.echocat.jomon.runtime.util.RangeSupport;
import org.echocat.jomon.runtime.util.SignificantableBasedOn;
import org.echocat.jomon.runtime.util.SignificantableWithMinAndMaxBasedOn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;

import static java.lang.Long.MAX_VALUE;

@ThreadSafe
@Immutable
@XmlJavaTypeAdapter(Adapter.class)
public class DateRange extends RangeSupport<Date> implements SignificantableWithMinAndMaxBasedOn<Date, Duration>, SignificantableBasedOn<Duration> {

    protected static final Date MIN_DATE = new Date(0);
    protected static final Date MAX_DATE = new Date(MAX_VALUE);

    public DateRange(@Nullable @Including Date from, @Nullable @Excluding Date to) {
        super(from, to);
    }

    @Override
    public boolean apply(@Nullable Date toTest) {
        return toTest != null && matchesFrom(toTest) && matchesTo(toTest);
    }

    protected boolean matchesFrom(@Nonnull Date toTest) {
        final Date from = getFrom();
        return from == null || from.before(toTest) || from.equals(toTest);
    }

    protected boolean  matchesTo(@Nonnull Date toTest) {
        final Date to = getTo();
        return to == null || to.after(toTest);
    }

    @Override
    public boolean isSignificant(@Nonnull Duration base) {
        return isSignificant(MIN_DATE, MAX_DATE, base);
    }

    @Override
    public boolean isSignificant(@Nonnull Date minValue, @Nonnull Date maxValue, @Nonnull Duration base) {
        final Date from = getFrom();
        final Date to = getTo();
        final Duration duration = new Duration(from != null ? from : minValue, to != null ? to : maxValue);
        return duration.isLessThanOrEqualTo(base);
    }

    @XmlRootElement(name = "dateRange")
    @XmlType(name = "dateRange")
    public static class Container {

        private Date _from;
        private Date _to;

        @XmlAttribute(name = "from")
        public Date getFrom() {
            return _from;
        }

        public void setFrom(Date from) {
            _from = from;
        }

        @XmlAttribute(name = "to")
        public Date getTo() {
            return _to;
        }

        public void setTo(Date to) {
            _to = to;
        }
    }

    public static class Adapter extends XmlAdapter<Container, DateRange> {

        @Override
        public DateRange unmarshal(Container v) throws Exception {
            final DateRange result;
            if (v != null) {
                result = new DateRange(v.getFrom(), v.getTo());
            } else {
                result = null;
            }
            return result;
        }

        @Override
        public Container marshal(DateRange v) throws Exception {
            final Container result;
            if (v != null) {
                result = new Container();
                result.setFrom(v.getFrom());
                result.setTo(v.getTo());
            } else {
                result = null;
            }
            return result;
        }
    }
}
