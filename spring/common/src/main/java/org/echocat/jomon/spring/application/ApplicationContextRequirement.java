/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.spring.application;

import org.echocat.jomon.runtime.generation.Requirement;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ApplicationContextRequirement extends Requirement {

    @Nullable
    public ApplicationContext getParentApplicationContext();

    @Nonnull
    public ClassLoader getClassLoader();

    public abstract static class Support<T extends Support<T>> implements ApplicationContextRequirement {

        @Nonnull
        private ClassLoader _classLoader = Thread.currentThread().getContextClassLoader();
        @Nullable
        private ApplicationContext _parentApplicationContext;

        public void setClassLoader(@Nonnull ClassLoader classLoader) {
            _classLoader = classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
        }

        @Nonnull
        public T withClassLoader(@Nonnull ClassLoader classLoader) {
            setClassLoader(classLoader);
            return thisObject();
        }

        @Override
        @Nonnull
        public ClassLoader getClassLoader() {
            return _classLoader;
        }

        @Override
        @Nullable
        public ApplicationContext getParentApplicationContext() {
            return _parentApplicationContext;
        }

        public void setParentApplicationContext(@Nullable ApplicationContext parentApplicationContext) {
            _parentApplicationContext = parentApplicationContext;
        }

        @Nonnull
        public T withParentApplicationContext(@Nullable ApplicationContext parent) {
            _parentApplicationContext = parent;
            return thisObject();
        }

        @Nonnull
        protected T thisObject() {
            //noinspection unchecked
            return (T) this;
        }

    }

}
