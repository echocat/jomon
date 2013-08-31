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

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;

import java.util.Iterator;
import java.util.List;

public class SnapshotIncludingArtifact extends DefaultArtifact {

    private boolean _snapshotsIncluded;

    public SnapshotIncludingArtifact(String groupId, String artifactId, VersionRange versionRange, String scope, String type, String classifier, ArtifactHandler artifactHandler, boolean snapshotsIncluded) {
        super(groupId, artifactId, versionRange, scope, type, classifier, artifactHandler);
        _snapshotsIncluded = snapshotsIncluded;
    }

    @Override
    public void setAvailableVersions(List<ArtifactVersion> versions) {
        // noinspection unchecked
        final Iterator<ArtifactVersion> i = versions.iterator();
        while (i.hasNext()) {
            final ArtifactVersion version = i.next();
            if (!_snapshotsIncluded && version != null && version.toString().endsWith("-SNAPSHOT")) {
                i.remove();
            }
        }
        super.setAvailableVersions(versions);
    }

    public boolean isSnapshotsIncluded() {
        return _snapshotsIncluded;
    }

    public void setSnapshotsIncluded(boolean snapshotsIncluded) {
        _snapshotsIncluded = snapshotsIncluded;
    }
}
