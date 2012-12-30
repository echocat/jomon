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

package org.echocat.jomon.runtime.valuemodule;

import org.echocat.jomon.runtime.valuemodule.access.ValueModuleToAssignmentStack;
import org.echocat.jomon.runtime.valuemodule.annotation.AnnotationBasedValueModuleToAssignmentStackFactory;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.BeanA;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.BeanAModule;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.a.BeanAAModule;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.a.a.BeanAAAModule;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class AnnotationBasedValueModuleAssignmentFactoryUnitTest {

    @Test
    public void testGetModuleToAssignmentStack() throws Exception {
        final AnnotationBasedValueModuleToAssignmentStackFactory factory = new AnnotationBasedValueModuleToAssignmentStackFactory();
        final ValueModuleToAssignmentStack<ValueModule, Object> stack = factory.<Object>getStack(BeanA.class);
        assertThat(stack.findBy(BeanAModule.a), matches(
            assignment(BeanAModule.a, "a")
        ));
        assertThat(stack.findBy(BeanAModule.b), matches(
            assignment(BeanAModule.b, "b")
        ));
        assertThat(stack.findBy(BeanAAModule.a), matches(
            assignment(BeanAModule.a, "a"),
            assignment(BeanAAModule.a, "a")
        ));
        assertThat(stack.findBy(BeanAAModule.b), matches(
            assignment(BeanAModule.a, "a"),
            assignment(BeanAAModule.b, "b")
        ));
        assertThat(stack.findBy(BeanAAAModule.a), matches(
            assignment(BeanAModule.a, "a"),
            assignment(BeanAAModule.a, "a"),
            assignment(BeanAAAModule.a, "a")
        ));
        assertThat(stack.findBy(BeanAAAModule.b), matches(
            assignment(BeanAModule.a, "a"),
            assignment(BeanAAModule.a, "a"),
            assignment(BeanAAAModule.b, "b")
        ));
    }

    @Nonnull
    protected static ValueModuleAssignment<? extends ValueModule, ?> assignment(@Nonnull ValueModule module, @Nonnull String propertyName) {
        final PropertyDescriptor descriptor = mock(PropertyDescriptor.class);
        doReturn(propertyName).when(descriptor).getName();
        return new ValueModuleAssignment<>(module, Object.class, descriptor, Collections.<ValueModuleAssignment<? extends ValueModule, ?>>emptySet());
    }

    @Nonnull
    protected static Matcher<Deque<ValueModuleAssignment<? extends ValueModule, ?>>> matches(@Nullable final ValueModuleAssignment<? extends ValueModule, ?>... assignments) {
        return new TypeSafeMatcher<Deque<ValueModuleAssignment<? extends ValueModule,  ?>>>() {
            @Override
            public boolean matchesSafely(@Nullable Deque<ValueModuleAssignment<? extends ValueModule, ?>> item) {
                boolean result;
                if (assignments == null || assignments.length == 0) {
                    result = item == null || item.isEmpty();
                } else if (item != null && item.size() == assignments.length) {
                    result = true;
                    int i = 0;
                    for (ValueModuleAssignment<? extends ValueModule, ?> current : item) {
                        final ValueModuleAssignment<? extends ValueModule, ?> expected = assignments[i++];
                        if (!expected.getModule().equals(current.getModule()) || !expected.getDescriptor().getName().equals(current.getDescriptor().getName())) {
                            result = false;
                        }
                    }
                } else {
                    result = false;
                }
                return result;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is ").appendValue(Arrays.toString(assignments));
            }
        };
    }


    protected static class Stack {

        private final ValueModule _module;
        private final Deque<ValueModuleAssignment<? extends ValueModule, ?>> _stack;

        public Stack(@Nonnull ValueModule module, @Nonnull Deque<ValueModuleAssignment<? extends ValueModule, ?>> stack) {
            _stack = stack;
            _module = module;
        }

        @Override
        public boolean equals(Object o) {
            final boolean result;
            if (this == o) {
                result = true;
            } else if (!(o instanceof Stack)) {
                result = false;
            } else {
                final Stack that = (Stack) o;
                result = _module.equals(that._module) && _stack.equals(that._stack);
            }
            return result;
        }

        @Override
        public int hashCode() {
            int result = _module.hashCode();
            result = 31 * result + _stack.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{module=" + _module + ", stack=" + _stack + "}";
        }
    }

}
