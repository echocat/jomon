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
import java.io.File;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class ArtifactManifestFactory {

    public ArtifactManifest getFor(@Nonnull Artifact artifact) throws Exception {
        final File file = artifact.getFile();
        if (file == null) {
            throw new RuntimeException("There was no file downloaded for " + artifact + "?");
        }
        final Manifest manifest;
        try (final JarFile jarFile = new JarFile(file)) {
            manifest = jarFile.getManifest();
        }
        return new ArtifactManifest(manifest, artifact);
    }
}
