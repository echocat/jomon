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

import org.apache.maven.project.*;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;
import java.util.Map.Entry;

import static java.util.Arrays.asList;

public class MavenProjectWithModulesFactory {
    
    @Nonnull
    public MavenProjectWithModules createFor(@Nonnull MavenEnvironment environment, @Nonnull File pomFile) throws Exception {
        final ProjectBuildingRequest request = createRequest(environment);
        return createProjectBy(environment, pomFile, request);
    }

    @Nonnull
    protected ProjectBuildingRequest createRequest(@Nonnull MavenEnvironment environment) {
        final DefaultProjectBuildingRequest request = new DefaultProjectBuildingRequest();
        request.setLocalRepository(environment.getRequest().getLocalRepository());
        request.setRemoteRepositories(environment.getRequest().getRemoteRepositories());
        request.setProfiles(environment.getRequest().getProfiles());
        request.setSystemProperties(environment.getRequest().getSystemProperties());
        request.setUserProperties(environment.getRequest().getUserProperties());
        request.setRepositorySession(environment.getRepositorySystemSession());
        return request;
    }

    @Nonnull
    protected MavenProjectWithModules createProjectBy(@Nonnull MavenEnvironment environment, @Nonnull File pomFile, @Nonnull ProjectBuildingRequest request) throws Exception {
        final List<ProjectBuildingResult> results = environment.getProjectBuilder().build(asList(pomFile), true, request);
        return resultsToProject(pomFile, results);
    }

    @Nonnull
    protected MavenProjectWithModules resultsToProject(@Nonnull File basePomFile, @Nonnull List<ProjectBuildingResult> results) throws Exception {
        final Map<MavenProject, MavenProjectWithModules> projectToProjectWithModules = new HashMap<>();
        for (final ProjectBuildingResult result : results) {
            if (!result.getProblems().isEmpty()) {
                throw new ProjectBuildingException(results);
            }
            final MavenProjectWithModules projectWithModules = getMavenProjectWithModules(result, projectToProjectWithModules);
            resolveModulesFor(projectWithModules, result, results, projectToProjectWithModules);
        }
        return selectBaseProject(basePomFile.getCanonicalFile(), projectToProjectWithModules);
    }

    private MavenProjectWithModules selectBaseProject(File by, Map<MavenProject, MavenProjectWithModules> from) throws Exception {
        MavenProjectWithModules result = null;
        for (final Entry<MavenProject, MavenProjectWithModules> projectAndProjectWithModules : from.entrySet()) {
            final MavenProject project = projectAndProjectWithModules.getKey();
            if (project.getFile() != null && by.equals(project.getFile().getCanonicalFile())) {
                result = projectAndProjectWithModules.getValue();
                break;
            }
        }
        if (result == null) {
            throw new IllegalStateException("Could not find any project for base pom '" + by + "' in " + from.values() + ".");
        }
        return result;
    }

    @Nonnull
    protected void resolveModulesFor(@Nonnull MavenProjectWithModules of, @Nonnull ProjectBuildingResult forResult, @Nonnull List<ProjectBuildingResult> fromResults, @Nonnull Map<MavenProject, MavenProjectWithModules> withProjectToProjectWithModules) throws Exception {
        for (final ProjectBuildingResult result : fromResults) {
            if (!result.getProblems().isEmpty()) {
                throw new ProjectBuildingException(fromResults);
            }
            for (final String moduleName : forResult.getProject().getModules()) {
                final File pomFile = result.getPomFile();
                final File expectedFile = new File(forResult.getPomFile().getParentFile(), moduleName + File.separator + pomFile.getName());
                if (expectedFile.getCanonicalFile().equals(pomFile.getCanonicalFile())) {
                    of.addModule(getMavenProjectWithModules(result, withProjectToProjectWithModules));
                    break;
                }
            }

        }
    }

    @Nonnull
    protected MavenProjectWithModules getMavenProjectWithModules(@Nonnull ProjectBuildingResult forResult, @Nonnull Map<MavenProject, MavenProjectWithModules> fromProjectToProjectWithModules) {
        final MavenProject project = forResult.getProject();
        MavenProjectWithModules projectWithModules = fromProjectToProjectWithModules.get(project);
        if (projectWithModules == null) {
            projectWithModules = new MavenProjectWithModules(project);
            fromProjectToProjectWithModules.put(project, projectWithModules);
        }
        return projectWithModules;
    }
}
