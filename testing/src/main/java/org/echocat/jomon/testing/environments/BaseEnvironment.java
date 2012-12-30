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

package org.echocat.jomon.testing.environments;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Closeable;

public abstract class BaseEnvironment implements Closeable {
    
    protected BaseEnvironment() {}

    @Nullable
    protected String findFileFor(@Nonnull Class<?> aClass, @Nonnull String fileNameSuffixOfClasses, @Nonnull String fileNameInPackage) {
        final String configuration;
        final ClassLoader classLoader = aClass.getClassLoader();
        final String configurationOfClass = getFileNameOfClass(aClass, fileNameSuffixOfClasses);
        if (classLoader.getResource(configurationOfClass) != null) {
            configuration = configurationOfClass;
        } else {
            configuration = findFileFor(aClass.getPackage(), classLoader, fileNameInPackage);
        }
        return configuration;
    }

    @Nonnull
    protected String getFileNameOfClass(@Nonnull Class<?> aClass, String configurationFileNameSuffixOfClasses) {
        return aClass.getName().replace('.', '/') + configurationFileNameSuffixOfClasses;
    }

    @Nullable
    protected String findFileFor(@Nonnull Package aPackage, @Nonnull ClassLoader classLoader, @Nonnull String fileNameInPackage) {
        String configuration = null;
        String current = aPackage.getName();
        while (configuration == null && current != null) {
            final String configurationOfPackage = getFileNameOfPackage(current, fileNameInPackage);
            if (classLoader.getResource(configurationOfPackage) != null) {
                configuration = configurationOfPackage;
            } else {
                current = getParentPackageName(current);
            }
        }
        return configuration;
    }

    @Nonnull
    protected String getFileNameOfPackage(@Nonnull String packageName, @Nonnull String fileNameInPackage) {
        return packageName.replace('.', '/') + "/" + fileNameInPackage;
    }

    @Nullable
    protected String getParentPackageName(@Nonnull String packageName) {
        final String parentPackageName;
        final int lastDot = packageName.lastIndexOf('.');
        if (lastDot > 0) {
            parentPackageName = packageName.substring(0, lastDot);
        } else {
            parentPackageName = null;
        }
        return parentPackageName;
    }
}
