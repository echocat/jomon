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

import org.apache.maven.project.MavenProject;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class MavenProjectWithModules {
    
    private final MavenProject _project;
    private final Collection<MavenProjectWithModules> _modules = new ArrayList<>();

    protected MavenProjectWithModules(@Nonnull MavenProject project) {
        _project = project;
    }

    @Nonnull
    public MavenProject getProject() {
        return _project;
    }

    @Nonnull
    public Collection<MavenProjectWithModules> getModules() {
        return Collections.unmodifiableCollection(_modules);
    }

    @Nonnull
    protected void addModule(MavenProjectWithModules mavenProjectWithModules) {
        _modules.add(mavenProjectWithModules);
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (o == null || getClass() != o.getClass()) {
            result = false;
        } else {
            final MavenProjectWithModules that = (MavenProjectWithModules) o;
            result = _project.equals(that._project);
        }
        return result;
    }

    @Override
    public int hashCode() {
        return _project.hashCode();
    }

    @Override
    public String toString() {
        return _project.toString();
    }
}
