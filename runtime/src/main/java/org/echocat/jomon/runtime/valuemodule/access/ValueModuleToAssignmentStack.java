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

package org.echocat.jomon.runtime.valuemodule.access;

import org.echocat.jomon.runtime.iterators.ConvertingIterator;
import org.echocat.jomon.runtime.util.Entry;
import org.echocat.jomon.runtime.util.Entry.Impl;
import org.echocat.jomon.runtime.valuemodule.ValueModule;
import org.echocat.jomon.runtime.valuemodule.ValueModuleAssignment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.PropertyDescriptor;
import java.util.*;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

public class ValueModuleToAssignmentStack<VM extends ValueModule, B> implements Iterable<Entry<VM, Deque<ValueModuleAssignment<? extends VM, ? extends B>>>> {

    private final Map<VM, Deque<ValueModuleAssignment<? extends VM, ? extends B>>> _moduleToAssignment;
    private final Set<Class<? extends VM>> _valueModuleTypes;
    private final Map<PropertyDescriptor, ValueModuleAssignment<? extends VM, ? extends B>> _descriptorToAssignment;
    private final Set<Class<? extends B>> _typesResponsibleFor;

    public ValueModuleToAssignmentStack(@Nonnull Map<? extends VM, Deque<ValueModuleAssignment<? extends VM, ? extends B>>> moduleToAssignment) {
        _moduleToAssignment = unmodifiableMap(moduleToAssignment);
        _valueModuleTypes = getValueModuleTypesBy(moduleToAssignment.keySet());
        _descriptorToAssignment = getDescriptorToAssignmentFor(moduleToAssignment);
        _typesResponsibleFor = getTypesResponsibleForBy(moduleToAssignment);
    }

    @Nonnull
    protected Set<Class<? extends VM>> getValueModuleTypesBy(@Nonnull Set<? extends VM> valueModules) {
        final Set<Class<? extends VM>> result = new HashSet<>();
        for (ValueModule valueModule : valueModules) {
            // noinspection unchecked
            result.add((Class)valueModule.getClass());
        }
        return unmodifiableSet(result);
    }

    @Nonnull
    protected Map<PropertyDescriptor, ValueModuleAssignment<? extends VM, ? extends B>> getDescriptorToAssignmentFor(@Nonnull Map<? extends VM, Deque<ValueModuleAssignment<? extends VM, ? extends B>>> moduleToAssignment) {
        final Map<PropertyDescriptor, ValueModuleAssignment<? extends VM, ? extends B>> descriptorToAssignment = new HashMap<>();
        for (Deque<ValueModuleAssignment<? extends VM, ? extends B>> valueModuleAssignments : moduleToAssignment.values()) {
            for (ValueModuleAssignment<? extends VM, ? extends B> valueModuleAssignment : valueModuleAssignments) {
                descriptorToAssignment.put(valueModuleAssignment.getDescriptor(), valueModuleAssignment);
            }
        }
        return unmodifiableMap(descriptorToAssignment);
    }

    @Nonnull
    protected Set<Class<? extends B>> getTypesResponsibleForBy(@Nonnull Map<? extends VM, Deque<ValueModuleAssignment<? extends VM, ? extends B>>> moduleToAssignment) {
        final Set<Class<? extends B>> typesResponsibleFor = new HashSet<>();
        for (Deque<ValueModuleAssignment<? extends VM, ? extends B>> assignments : moduleToAssignment.values()) {
            for (ValueModuleAssignment<? extends VM, ? extends B> assignment : assignments) {
                typesResponsibleFor.add(assignment.getBound());
            }
        }
        return unmodifiableSet(typesResponsibleFor);
    }

    public boolean isResponsibleFor(@Nonnull Class<?> type) {
        // noinspection SuspiciousMethodCalls
        return _typesResponsibleFor.contains(type);
    }

    @Nonnull
    public Set<Class<? extends B>> getTypesResponsibleFor() {
        return _typesResponsibleFor;
    }

    @Nullable
    public Deque<ValueModuleAssignment<? extends VM, ? extends B>> findBy(@Nonnull VM module) {
        return _moduleToAssignment.get(module);
    }

    @Nullable
    public ValueModuleAssignment<? extends VM, ? extends B> findAssignmentBy(@Nonnull PropertyDescriptor descriptor) {
        return _descriptorToAssignment.get(descriptor);
    }

    @Nonnull
    public Set<Class<? extends VM>> getAllSupportedValueModuleTypes() {
        return _valueModuleTypes;
    }

    @Nonnull
    public Set<? extends VM> getAllSupportedValueModules() {
        return _moduleToAssignment.keySet();
    }

    @Override
    public Iterator<Entry<VM, Deque<ValueModuleAssignment<? extends VM, ? extends B>>>> iterator() {
        return new ConvertingIterator<Map.Entry<VM, Deque<ValueModuleAssignment<? extends VM, ? extends B>>>, Entry<VM, Deque<ValueModuleAssignment<? extends VM, ? extends B>>>>(_moduleToAssignment.entrySet().iterator()) { @Override protected Entry<VM, Deque<ValueModuleAssignment<? extends VM, ? extends B>>> convert(Map.Entry<VM, Deque<ValueModuleAssignment<? extends VM, ? extends B>>> input) {
            final VM valueModule = input.getKey();
            final Deque<ValueModuleAssignment<? extends VM, ? extends B>> assignments = input.getValue();
            return new Impl<>(valueModule, assignments);
        }};
    }
}
