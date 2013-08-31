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

package org.echocat.jomon.maven;

import org.apache.commons.io.IOUtils;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.annotation.Nonnull;
import java.io.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

public class TestPom extends TemporaryFolder {

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public @interface NameOfPomFileIs {
        public String[] value();
    }

    private File[] _files;
    private Description _description;

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() { @Override public void evaluate() throws Throwable {
            before();
            final File[] files = createPomsFor(description, getRoot());
            try {
                _description = description;
                _files = files;
                base.evaluate();
            } finally {
                _files = null;
                _description = null;
                after();
            }
        }};
    }

    @Nonnull
    public File getFile() {
        if (_description == null) {
            throw new IllegalStateException("Use this method only in combination with a junit rule.");
        }
        if (_files == null) {
            throw new IllegalStateException(_description.getMethodName() + " was not annotated with " + NameOfPomFileIs.class.getName() + ".");
        }
        return _files[0];
    }

    @Nonnull
    private Method getTestMethodFor(Description description) {
        final Method method;
        try {
            method = description.getTestClass().getDeclaredMethod(description.getMethodName());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(description + " does not contain a valid test definition.", e);
        }
        return method;
    }

    @Nonnull
    private File[] createPomsFor(@Nonnull Description description, @Nonnull File root) throws IOException {
        final Method method = getTestMethodFor(description);
        final File[] result;
        final NameOfPomFileIs nameOfPomFile = method.getAnnotation(NameOfPomFileIs.class);
        if (nameOfPomFile != null && nameOfPomFile.value().length > 0) {
            result = new File[nameOfPomFile.value().length];
            for (int i=0; i<nameOfPomFile.value().length; i++) {
                final String fileName = nameOfPomFile.value()[i];
                result[i] = new File(root, fileName);
                result[i].getParentFile().mkdirs();
                try (final InputStream is = description.getTestClass().getResourceAsStream(fileName)) {
                    try (final OutputStream os = new FileOutputStream(result[i])) {
                        IOUtils.copy(is, os);
                    }
                }
            }
        } else {
            result = null;
        }
        return result;
    }

}
