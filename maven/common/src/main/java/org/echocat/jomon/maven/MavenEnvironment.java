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

import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataManager;
import org.apache.maven.artifact.resolver.ResolutionErrorHandler;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.project.ProjectBuilder;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.impl.ArtifactResolver;

import javax.annotation.Nonnull;

public class MavenEnvironment {

    private final MavenExecutionRequest _request;
    private final RepositorySystemSession _repositorySystemSession;

    private final ArtifactResolver _artifactResolver;
    private final ArtifactHandlerManager _artifactHandlerManager;
    private final RepositoryMetadataManager _repositoryMetadataManager;
    private final ProjectBuilder _projectBuilder;
    private final ResolutionErrorHandler _resolutionErrorHandler;

    public MavenEnvironment(@Nonnull DefaultPlexusContainer container, @Nonnull MavenExecutionRequest request, @Nonnull RepositorySystemSession repositorySystemSession) throws Exception {
        _request = request;
        _repositorySystemSession = repositorySystemSession;

        _artifactResolver = container.lookup(ArtifactResolver.class);
        _artifactHandlerManager = container.lookup(ArtifactHandlerManager.class);
        _repositoryMetadataManager = container.lookup(RepositoryMetadataManager.class);
        _projectBuilder = container.lookup(ProjectBuilder.class);
        _resolutionErrorHandler = container.lookup(ResolutionErrorHandler.class);

    }

    public MavenEnvironment(@Nonnull MavenExecutionRequest request, @Nonnull RepositorySystemSession repositorySystemSession, @Nonnull ArtifactResolver artifactResolver, @Nonnull ArtifactHandlerManager artifactHandlerManager, @Nonnull RepositoryMetadataManager repositoryMetadataManager, @Nonnull ProjectBuilder projectBuilder, @Nonnull ResolutionErrorHandler resolutionErrorHandler) {
        _request = request;
        _repositorySystemSession = repositorySystemSession;
        _artifactResolver = artifactResolver;
        _artifactHandlerManager = artifactHandlerManager;
        _repositoryMetadataManager = repositoryMetadataManager;
        _projectBuilder = projectBuilder;
        _resolutionErrorHandler = resolutionErrorHandler;
    }

    @Nonnull
    public MavenExecutionRequest getRequest() {
        return _request;
    }

    @Nonnull
    public RepositorySystemSession getRepositorySystemSession() {
        return _repositorySystemSession;
    }

    @Nonnull
    public ArtifactResolver getArtifactResolver() {
        return _artifactResolver;
    }

    @Nonnull
    public ArtifactHandlerManager getArtifactHandlerManager() {
        return _artifactHandlerManager;
    }

    @Nonnull
    public RepositoryMetadataManager getRepositoryMetadataManager() {
        return _repositoryMetadataManager;
    }

    @Nonnull
    public ProjectBuilder getProjectBuilder() {
        return _projectBuilder;
    }

    @Nonnull
    public ResolutionErrorHandler getResolutionErrorHandler() {
        return _resolutionErrorHandler;
    }
}
