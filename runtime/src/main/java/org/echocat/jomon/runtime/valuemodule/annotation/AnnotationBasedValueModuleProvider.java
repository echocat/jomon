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

package org.echocat.jomon.runtime.valuemodule.annotation;

import org.echocat.jomon.runtime.StringUtils;
import org.echocat.jomon.runtime.valuemodule.ValueModule;
import org.echocat.jomon.runtime.valuemodule.ValueModuleProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.echocat.jomon.runtime.valuemodule.ValueModuleUtils.valuesOf;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class AnnotationBasedValueModuleProvider<VM extends Enum<VM> & ValueModule> implements ValueModuleProvider<VM> {

    private final Map<String, VM> _idToModule;

    public AnnotationBasedValueModuleProvider(@Nonnull Class<? extends VM>... types) {
        this(types != null ? asList(types) : null);
    }

    public AnnotationBasedValueModuleProvider(@Nonnull Collection<Class<? extends VM>> types) {
        if (types == null || types.isEmpty()) {
            throw new IllegalArgumentException("Provide minimum of one types.");
        }
        _idToModule = unmodifiableMap(getAllModulesOf(types));
    }

    @Nonnull
    protected Map<String, VM> getAllModulesOf(@Nonnull Collection<Class<? extends VM>> types) {
        final Map<String, VM> modules = new LinkedHashMap<>();
        for (Class<? extends VM> type : types) {
            // noinspection RedundantCast, unchecked
            for (VM value : valuesOf((Class<VM>) (Object) type)) {
                assertType(type);
                modules.put(value.getId(), value);
            }
        }
        return modules;
    }

    protected void assertType(@Nonnull Class<? extends ValueModule> type) {
        if (!ValueModule.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException(type.getName() + " is not of type " + ValueModule.class.getName() + ".");
        }
        if (!type.isEnum()) {
            throw new IllegalArgumentException(type.getName() + " is not a enum.");
        }
    }

    @Nonnull
    @Override
    public VM provideBy(@Nonnull String id) {
        final VM vm = _idToModule.get(id);
        if (vm == null) {
            throw new IllegalArgumentException(id);
        }
        return vm;
    }

    @Nonnull
    @Override
    public Iterable<? extends VM> getAll() {
        return _idToModule.values();
    }


    @Nonnull
    @Override
    public Set<? extends VM> provideAllBy(@Nullable String commaSeparatedIds) throws IllegalArgumentException {
        return provideAllBy(splitIds(commaSeparatedIds));
    }

    @Nonnull
    private Set<? extends VM> provideAllBy(@Nullable Iterable<String> ids) throws IllegalArgumentException {
        final Set<VM> modules = new HashSet<>();
        if (ids != null) {
            for (String id : ids) {
                final VM module = provideBy(id);
                modules.add(module);
            }
        }
        return unmodifiableSet(modules);
    }

    @Override
    @Nonnull
    public String toCommaSeparatedIds(@Nullable Set<? extends VM> valueModules) {
        final StringBuilder sb = new StringBuilder();
        if (valueModules != null) {
            for (VM valueModule : valueModules) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(valueModule.getId());
            }
        }
        return sb.toString();
    }

    @Nullable
    protected Iterable<String> splitIds(@Nullable String commaSeparatedIds) {
        final Set<String> result;
        if (!isEmpty(commaSeparatedIds)) {
            result = new HashSet<>();
            for (String plainModule : StringUtils.split(commaSeparatedIds, ',')) {
                final String trimmedPlainModule = plainModule.trim();
                if (!trimmedPlainModule.isEmpty()) {
                    result.add(trimmedPlainModule);
                }
            }
        } else {
            result = null;
        }

        return result != null && !result.isEmpty() ? result : null;
    }

}
