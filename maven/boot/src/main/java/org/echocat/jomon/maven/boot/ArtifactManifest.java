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

package org.echocat.jomon.maven.boot;

import org.apache.maven.artifact.Artifact;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class ArtifactManifest extends Manifest {
    
    private static final String MAIN_CLASS_ATTRIBUTE = "Main-Class";

    private final Artifact _artifact;

    public ArtifactManifest(Manifest manifest, Artifact artifact) {
        super(manifest);
        _artifact = artifact;
    }

    @Nonnull
    public Artifact getArtifact() {
        return _artifact;
    }

    @Nullable
    public Class<?> getMainClass(@Nonnull ClassLoader classLoader) {
        Class<?> result = null;
        final Attributes attributes = getMainAttributes();
        if (attributes != null) {
            final String mainClassName = attributes.getValue(MAIN_CLASS_ATTRIBUTE);
            if (mainClassName != null) {
                try {
                    result = classLoader.loadClass(mainClassName);
                } catch (final ClassNotFoundException e) {
                    //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                    throw new IllegalStateException("The mainClass '" + mainClassName + "' specified in manifest of " + _artifact + " could not be found in " + classLoader + ".");
                }
            }
        }
        return result;
    }
    
    @Override
    public Object clone() {
        return super.clone();
    }
}
