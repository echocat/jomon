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

import org.echocat.jomon.runtime.generation.Generator;
import org.echocat.jomon.runtime.generation.StringGenerator;
import org.echocat.jomon.runtime.iterators.CloseableIterator;
import org.echocat.jomon.runtime.numbers.LongGenerator;
import org.echocat.jomon.runtime.repository.QueryableRepository;
import org.echocat.jomon.runtime.repository.RemovingRepository;
import org.echocat.jomon.runtime.repository.UpdateableRepository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.google.common.collect.Sets.filter;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.echocat.jomon.demo.repository.EmployeeQuery.query;
import static org.echocat.jomon.runtime.CollectionUtils.*;

@SuppressWarnings("deprecation")
public class EmployeeRepository implements Generator<Employee, EmployeeRequirement>, QueryableRepository<EmployeeQuery, Long, Employee>, RemovingRepository<EmployeeQuery, Long>, UpdateableRepository<EmployeeQuery, Long, EmployeeUpdate> {

    private static final StringGenerator NAME_GENERATOR = new StringGenerator();
    private static final LongGenerator ID_GENERATOR = new LongGenerator();

    private final Set<Employee> _employees = new HashSet<>();

    @Nonnull
    @Override
    public Employee generate(@Nonnull EmployeeRequirement requirement) {
        final Employee employee = generateInstanceBy(requirement);
        _employees.add(employee);
        return employee;
    }

    @Nonnull
    protected Employee generateInstanceBy(EmployeeRequirement requirement) {
        final Employee employee = new Employee();
        employee.setId(ID_GENERATOR.generate(requirement.getId()));
        employee.setName(NAME_GENERATOR.generate(requirement.getName()));
        employee.setLastModification(new Date());
        employee.setJoined(new Date());
        return employee;
    }

    @Override
    public Employee findOneBy(@Nonnull Long id) {
        return findOneBy(query().withId(id));
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
        return removeBy(query().withId(id)) > 0;
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

    @Nullable
    @Override
    public boolean update(@Nonnull EmployeeUpdate what, @Nonnull Long byId) {
        final Employee employee = findOneBy(byId);
        final boolean updated;
        if (employee != null) {
            update(what, employee);
            updated = true;
        } else {
            updated = false;
        }
        return updated;
    }

    @Nullable
    @Override
    public long update(@Nonnull EmployeeUpdate what, @Nonnull EmployeeQuery byQuery) {
        long updated = 0;
        try (final CloseableIterator<Employee> i = findBy(byQuery)) {
            while (i.hasNext()) {
                final Employee employee = i.next();
                update(what, employee);
                updated++;
            }
        }
        return updated;
    }

    protected void update(@Nonnull EmployeeUpdate what, @Nonnull Employee on) {
        updateName(what, on);
        updateNumberOfLogins(what, on);
        updateLastModified(on);
    }

    protected void updateName(@Nonnull EmployeeUpdate what, @Nonnull Employee on) {
        final String name = what.getName();
        if (!isEmpty(name)) {
            on.setName(name);
        }
    }

    protected void updateNumberOfLogins(@Nonnull EmployeeUpdate what, @Nonnull Employee on) {
        if (what.isIncrementNumberOfLogins()) {
            on.setNumberOfLogins(on.getNumberOfLogins() + 1);
        }
    }

    protected void updateLastModified(@Nonnull Employee on) {
        on.setLastModification(new Date());
    }

}
