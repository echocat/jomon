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

import static org.echocat.jomon.demo.repository.EmployeeRequirement.requirement;
import static org.echocat.jomon.demo.repository.EmployeeUpdate.update;
import static org.echocat.jomon.runtime.CollectionUtils.asList;
import static org.echocat.jomon.testing.BaseMatchers.is;
import static org.echocat.jomon.testing.IterableMatchers.containsAllItemsOf;
import static org.echocat.jomon.demo.repository.EmployeeQuery.query;
import static org.junit.Assert.assertThat;

public class EmployeeRepositoryUnitTest {

    @Test
    public void testOnePlusOne() throws Exception {
        final EmployeeRepository repository = new EmployeeRepository();

        assertThat(repository.findOneBy(1L), is(null));
        assertThat(repository.findOneBy(query().withName("bert")), is(null));
        assertThat(repository.countBy(query().withName("bert")), is(0L));

        final Employee bert = repository.generate(requirement().withName("bert").withId(1));
        assertThat(repository.findOneBy(1L), is(bert));
        assertThat(repository.findOneBy(query().withName("bert")), is(bert));
        assertThat(repository.countBy(query().withName("bert")), is(1L));

        assertThat(repository.findOneBy(1L).getNumberOfLogins(), is(0));
        repository.update(update().incrementNumberOfLogins(), query().withId(1L));
        assertThat(repository.findOneBy(1L).getNumberOfLogins(), is(1));

        repository.generate(requirement().withName("bettina").withId(2));
        assertThat(repository.findOneBy(1L), is(bert));
        assertThat(repository.findOneBy(query().withName("bert")), is(bert));
        assertThat(repository.countBy(query().withName("bert")), is(1L));

        assertThat(repository.removeBy(1L), is(true));
        assertThat(repository.findOneBy(1L), is(null));
    }

    @Test
    public void testTwoPlusOne() throws Exception {
        final EmployeeRepository repository = new EmployeeRepository();

        assertThat(repository.findOneBy(1L), is(null));
        assertThat(repository.findOneBy(2L), is(null));
        assertThat(repository.findOneBy(3L), is(null));
        assertThat(repository.findOneBy(query().withNamePrefixedBy("be")), is(null));
        assertThat(repository.countBy(query().withNamePrefixedBy("be")), is(0L));

        final Employee bert = repository.generate(requirement().withName("bert").withId(1));
        final Employee bettina = repository.generate(requirement().withName("bettina").withId(2));
        final Employee philipp = repository.generate(requirement().withName("philipp").withId(3));
        assertThat(repository.findOneBy(1L), is(bert));
        assertThat(repository.findOneBy(2L), is(bettina));
        assertThat(repository.findOneBy(3L), is(philipp));
        assertThat(asList(repository.findBy(query().withNamePrefixedBy("be"))), containsAllItemsOf(bert, bettina));
        assertThat(repository.countBy(query().withNamePrefixedBy("be")), is(2L));

        assertThat(repository.removeBy(query().withNamePrefixedBy("be")), is(2L));
        assertThat(repository.countBy(query()), is(1L));
        assertThat(asList(repository.findBy(query())), containsAllItemsOf(philipp));
    }

}
