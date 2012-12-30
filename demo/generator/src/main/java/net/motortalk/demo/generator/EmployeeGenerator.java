/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package net.motortalk.demo.generator;

import org.echocat.jomon.runtime.generation.Generator;
import org.echocat.jomon.runtime.generation.StringGenerator;

import javax.annotation.Nonnull;
import java.util.Date;

import static org.echocat.jomon.runtime.date.DateGenerator.generateDate;

public class EmployeeGenerator implements Generator<Employee, EmployeeRequirement> {

    private static final EmployeeGenerator INSTANCE = new EmployeeGenerator();

    @Nonnull
    public static Employee generateAn(@Nonnull EmployeeRequirement requirement) {
        return INSTANCE.generate(requirement);
    }

    private StringGenerator _stringGenerator = new StringGenerator();

    @Nonnull
    @Override
    public Employee generate(@Nonnull EmployeeRequirement requirement) {
        final Employee employee = new Employee();
        handleName(requirement, employee);
        handleJoined(requirement, employee);
        return employee;
    }

    protected void handleName(@Nonnull EmployeeRequirement requirement, @Nonnull Employee employee) {
        final String name = _stringGenerator.generate(requirement.getName());
        employee.setName(name);
    }

    protected void handleJoined(@Nonnull EmployeeRequirement requirement, @Nonnull Employee employee) {
        final Date joined = generateDate(requirement.getJoined());
        employee.setJoined(joined);
    }

    public void setStringGenerator(@Nonnull StringGenerator stringGenerator) {
        _stringGenerator = stringGenerator;
    }
}
