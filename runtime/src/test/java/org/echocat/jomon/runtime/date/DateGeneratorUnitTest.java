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

import org.junit.Test;

import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DateGeneratorUnitTest {

    @Test
    public void testGenerateExactDate() throws Exception {
        final DateGenerator dateGenerator = new DateGenerator();
        final Date date1 = new Date(100000);
        final ExactDateRequirement exactDateRequirement = new ExactDateRequirement(date1);
        Date generatedDate = dateGenerator.generateExact(exactDateRequirement);
        assertThat(generatedDate, is(date1));
        generatedDate = dateGenerator.generate(exactDateRequirement);
        assertThat(generatedDate, is(date1));
    }

    @Test
    public void testGenerateDateRange() throws Exception {
        final DateGenerator dateGenerator = new DateGenerator();
        final Date date1 = new Date(100000);
        final Date date2 = new Date(200000);
        final DateRangeRequirement dateRangeRequirement = new DateRangeRequirement(date1, date2);
        Date generatedDate = dateGenerator.generateInRange(dateRangeRequirement);
        assertThat(isInRange(generatedDate, date1, date2), is(true));
        generatedDate = dateGenerator.generate(dateRangeRequirement);
        assertThat(isInRange(generatedDate, date1, date2), is(true));
    }

    private boolean isInRange(Date generatedDate, Date date1, Date date2) {
        return generatedDate.after(date1) || generatedDate.equals(date1) && generatedDate.before(date2);
    }
}
