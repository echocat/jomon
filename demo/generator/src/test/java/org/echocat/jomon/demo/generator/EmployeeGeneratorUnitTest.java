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

package org.echocat.jomon.demo.generator;

import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.Date;

import static java.lang.System.out;
import static org.echocat.jomon.runtime.DateTimeUtils.now;
import static org.echocat.jomon.testing.BaseMatchers.*;
import static org.echocat.jomon.testing.StringMatchers.startsWith;
import static org.echocat.jomon.demo.generator.EmployeeGenerator.generateAn;
import static org.echocat.jomon.demo.generator.EmployeeRequirement.employee;
import static org.junit.Assert.assertThat;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class EmployeeGeneratorUnitTest {

    @Test
    public void testGenerateOne() throws Exception {
        final Date now = now();
        final Employee employee = generateAn(employee().withNamePrefixedBy("bert-").joined(now));
        assertThat(employee.getName(), startsWith("bert-"));
        assertThat(employee.getJoined(), is(now));
        print(employee);
    }

    @Test
    public void testGenerateTwo() throws Exception {
        final Date now = now();
        final EmployeeRequirement requirement = employee().withNamePrefixedBy("philipp-").joinedAfter(now);
        final Employee employee1 = generateAn(requirement);
        assertThat(employee1.getName(), startsWith("philipp-"));
        assertThat(employee1.getJoined(), isGreaterThanOrEqualTo(now));
        print(employee1);

        final Employee employee2 = generateAn(requirement);
        assertThat(employee2.getName(), startsWith("philipp-"));
        assertThat(employee2.getJoined(), isGreaterThanOrEqualTo(now));
        print(employee2);
    }

    @Test
    public void testGenerateThree() throws Exception {
        final Date now = now();
        final EmployeeRequirement requirement = employee().withNamePrefixedBy("gregor-").joinedBefore(now);
        final Employee employee1 = generateAn(requirement);
        assertThat(employee1.getName(), startsWith("gregor-"));
        assertThat(employee1.getJoined(), isLessThan(now));
        print(employee1);

        final Employee employee2 = generateAn(requirement);
        assertThat(employee2.getName(), startsWith("gregor-"));
        assertThat(employee2.getJoined(), isLessThan(now));
        print(employee2);

        final Employee employee3 = generateAn(requirement);
        assertThat(employee3.getName(), startsWith("gregor-"));
        assertThat(employee3.getJoined(), isLessThan(now));
        print(employee3);
    }

    protected static void print(@Nonnull Employee employee) {
        out.println("Employee with name " + employee.getName() + " joined us at " + employee.getJoined() + ".");
    }

}
