/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2014 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.demo.repository;

import org.junit.Test;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static org.echocat.jomon.runtime.CollectionUtils.asList;
import static org.echocat.jomon.testing.BaseMatchers.is;
import static org.echocat.jomon.testing.IterableMatchers.containsAllItemsOf;
import static org.echocat.jomon.demo.repository.EmployeeQuery.employee;
import static org.junit.Assert.assertThat;

public class EmployeeRepositoryUnitTest {

    @Test
    public void testOnePlusOne() throws Exception {
        final Employee bert = employeeWith(1L, "bert");
        final Employee bettina = employeeWith(2L, "bettina");
        final EmployeeRepository repository = new EmployeeRepository();

        assertThat(repository.findOneBy(1L), is(null));
        assertThat(repository.findOneBy(employee().withName("bert")), is(null));
        assertThat(repository.countBy(employee().withName("bert")), is(0L));

        repository.insert(bert);
        assertThat(repository.findOneBy(1L), is(bert));
        assertThat(repository.findOneBy(employee().withName("bert")), is(bert));
        assertThat(repository.countBy(employee().withName("bert")), is(1L));

        repository.insert(bettina);
        assertThat(repository.findOneBy(1L), is(bert));
        assertThat(repository.findOneBy(employee().withName("bert")), is(bert));
        assertThat(repository.countBy(employee().withName("bert")), is(1L));

        assertThat(repository.removeBy(1L), is(true));
        assertThat(repository.findOneBy(1L), is(null));
    }

    @Test
    public void testTwoPlusOne() throws Exception {
        final Employee bert = employeeWith(1L, "bert");
        final Employee bettina = employeeWith(2L, "bettina");
        final Employee philipp = employeeWith(3L, "philipp");
        final EmployeeRepository repository = new EmployeeRepository();

        assertThat(repository.findOneBy(1L), is(null));
        assertThat(repository.findOneBy(2L), is(null));
        assertThat(repository.findOneBy(3L), is(null));
        assertThat(repository.findOneBy(employee().withNamePrefixedBy("be")), is(null));
        assertThat(repository.countBy(employee().withNamePrefixedBy("be")), is(0L));

        repository.insert(bert);
        repository.insert(bettina);
        repository.insert(philipp);
        assertThat(repository.findOneBy(1L), is(bert));
        assertThat(repository.findOneBy(2L), is(bettina));
        assertThat(repository.findOneBy(3L), is(philipp));
        assertThat(asList(repository.findBy(employee().withNamePrefixedBy("be"))), containsAllItemsOf(bert, bettina));
        assertThat(repository.countBy(employee().withNamePrefixedBy("be")), is(2L));

        assertThat(repository.removeBy(employee().withNamePrefixedBy("be")), is(2L));
        assertThat(repository.countBy(employee()), is(1L));
        assertThat(asList(repository.findBy(employee())), containsAllItemsOf(philipp));
    }

    @Nonnull
    protected static Employee employeeWith(@Nonnegative long id, @Nonnull String name) {
        final Employee employee = new Employee();
        employee.setName(name);
        employee.setId(id);
        return employee;
    }
}
