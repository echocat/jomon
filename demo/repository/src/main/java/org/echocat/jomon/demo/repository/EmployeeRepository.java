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

package org.echocat.jomon.demo.repository;

import org.echocat.jomon.runtime.iterators.CloseableIterator;
import org.echocat.jomon.runtime.repository.InsertingRepository;
import org.echocat.jomon.runtime.repository.QueryableRepository;
import org.echocat.jomon.runtime.repository.RemovingRepository;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.google.common.collect.Sets.filter;
import static org.echocat.jomon.runtime.CollectionUtils.*;
import static org.echocat.jomon.demo.repository.EmployeeQuery.employee;

public class EmployeeRepository implements InsertingRepository<Employee>, QueryableRepository<EmployeeQuery, Long, Employee>, RemovingRepository<EmployeeQuery, Long> {

    private final Set<Employee> _employees = new HashSet<>();

    @Override
    public void insert(@Nonnull Employee toInsert) {
        _employees.add(toInsert);
    }

    @Override
    public Employee findOneBy(@Nonnull Long id) {
        return findOneBy(employee().withId(id));
    }

    @Nonnull
    @Override
    public CloseableIterator<Employee> findBy(@Nonnull EmployeeQuery query) {
        return asIterator(filter(_employees, query));
    }

    @Override
    public Employee findOneBy(@Nonnull EmployeeQuery query) {
        return findFirstOf(findBy(query));
    }

    @Override
    public long countBy(@Nonnull EmployeeQuery query) {
        return countElementsOf(findBy(query));
    }

    @Override
    public boolean removeBy(@Nonnull Long id) {
        return removeBy(employee().withId(id)) > 0;
    }

    @Override
    public long removeBy(@Nonnull EmployeeQuery query) {
        final Iterator<Employee> i = _employees.iterator();
        long removed = 0;
        while (i.hasNext()) {
            final Employee employee = i.next();
            if (query.apply(employee)) {
                removed++;
                i.remove();
            }
        }
        return removed;
    }
}
