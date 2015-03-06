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

import org.echocat.jomon.maven.boot.ArtifactFactoryRequest.OptionalHandling;
import org.apache.maven.DefaultMaven;
import org.apache.maven.Maven;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.ArtifactRepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataManager;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.resolver.*;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.cli.MavenCli;
import org.apache.maven.cli.MavenLoggerManager;
import org.apache.maven.execution.*;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.project.*;
import org.apache.maven.wagon.shared.http.AbstractHttpClientWagon;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.transfer.TransferListener;
import org.sonatype.aether.util.DefaultRepositorySystemSession;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import static org.echocat.jomon.maven.boot.ArtifactFactoryRequest.OptionalHandling.*;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.maven.artifact.versioning.VersionRange.createFromVersion;
import static org.apache.maven.model.building.ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL;

/**
 * This factory is used to retrieve dynamically artifacts.
 */
public class ArtifactFactory {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MavenBoot.class);
    protected static final String BOOTSTRAP_POM_XML = "bootstrap.pom.xml";

    static {
        hackToPreventUselessWagonDebugOutputOnStdout();
    }

    private static void hackToPreventUselessWagonDebugOutputOnStdout() {
        try {
            // noinspection UseOfSystemOutOrSystemErr
            final PrintStream old = System.out;
            try {
                System.setOut(new PrintStream(new ByteArrayOutputStream()));
                new AbstractHttpClientWagon() {};
            } finally {
                System.setOut(old);
            }
        } catch (final Exception ignored) {
        }
    }

    private final ContainerAndRequest _containerAndRequest;
    private final ArtifactResolver _artifactResolver;
    private final ProjectBuilder _projectBuilder;
    private final ArtifactHandlerManager _artifactHandlerManager;
    private final RepositoryMetadataManager _repositoryMetadataManager;
    private final ResolutionErrorHandler  _resolutionErrorHandler;

    public ArtifactFactory() throws Exception {
        _containerAndRequest = createContainerAndRequest();
        _artifactResolver = lookup(ArtifactResolver.class);
        _artifactHandlerManager = lookup(ArtifactHandlerManager.class);
        _repositoryMetadataManager = lookup(RepositoryMetadataManager.class);
        _projectBuilder = lookup(ProjectBuilder.class);
        _resolutionErrorHandler = lookup(ResolutionErrorHandler .class);

        setLogger(new PlexusToSl4jLogger(LOG));
        setTransferListener(new Sl4jTransferListener(LOG));
    }

    private <T> T lookup(Class<T> type) throws ComponentLookupException {
        return _containerAndRequest.getContainer().lookup(type);
    }

    @Nonnull
    public ArtifactWithDependencies get(@Nonnull final ArtifactFactoryRequest request) throws Exception {
        final Artifact artifact = request.getArtifact();
        final MavenProject project = createProjectFor();
        resolve(project, artifact, request, false);
        return toArtifactWithDependencies(project, artifact, request);
    }

    @Nonnull
    protected MavenProject createProjectFor() throws ProjectBuildingException {
        final MavenExecutionRequest request = _containerAndRequest.getRequest();
        final ProjectBuildingRequest projectBuildingRequest = request.getProjectBuildingRequest();
        projectBuildingRequest.setValidationLevel(VALIDATION_LEVEL_MINIMAL);
        final ProjectBuildingResult build = _projectBuilder.build(createModelSource(), projectBuildingRequest);
        return build.getProject();
    }

    protected ModelSource createModelSource() {
        return new ModelSource() {
            @Override
            public InputStream getInputStream() throws IOException {
                final InputStream is = ArtifactFactory.class.getResourceAsStream(BOOTSTRAP_POM_XML);
                if (is == null) {
                    throw new FileNotFoundException("Could not find the " + BOOTSTRAP_POM_XML + ".");
                }
                return is;
            }

            @Override
            public String getLocation() {
                return BOOTSTRAP_POM_XML;
            }
        };
    }


    @Nonnull
    protected ArtifactResolutionResult resolve(@Nonnull MavenProject project, @Nonnull Artifact artifact, @Nonnull ArtifactFactoryRequest request, boolean resolveTransitively) throws Exception {
        if (artifact.getVersionRange() != null && artifact.getVersionRange().hasRestrictions()) {
            final String latestVersion = getLatestVersionOf(project, artifact);
            artifact.setVersionRange(createFromVersion(latestVersion));
        }

        final ArtifactResolutionRequest resolutionRequest = createRequestFor(project, artifact);
        resolutionRequest.setForceUpdate(request.isForceUpdate());
        resolutionRequest.setResolveTransitively(resolveTransitively);
        resolutionRequest.setResolutionFilter(toArtifactFilter(request));
        final ArtifactResolutionResult resolutionResult = _artifactResolver.resolve(resolutionRequest);
        _resolutionErrorHandler.throwErrors(resolutionRequest, resolutionResult);
        return resolutionResult;
    }

    @Nonnull
    protected ArtifactFilter toArtifactFilter(@Nonnull ArtifactFactoryRequest request) {
        final ArtifactFilter includeScopesFilter = toIncludeScopesFilter(request.getIncludeScopes());
        final ArtifactFilter excludeScopesFilter = toExcludeScopesFilter(request.getExcludeScopes());
        final ArtifactFilter optionalFilter = toOptionalFilter(request.getOptionalHandling());
        return new AndArtifactFilter(Arrays.asList(includeScopesFilter, excludeScopesFilter, optionalFilter));
        
    }

    @Nonnull
    protected ArtifactFilter toIncludeScopesFilter(@Nonnull final Set<String> includeScopes) {
        return new ArtifactFilter() { @Override public boolean include(@Nonnull Artifact artifact) {
            return includeScopes.isEmpty() || includeScopes.contains(artifact.getScope());
        }};
    }

    @Nonnull
    protected ArtifactFilter toExcludeScopesFilter(@Nonnull final Set<String> excludeScopes) {
        return new ArtifactFilter() { @Override public boolean include(@Nonnull Artifact artifact) {
            return excludeScopes.isEmpty() || !excludeScopes.contains(artifact.getScope());
        }};
    }

    @Nonnull
    protected ArtifactFilter toOptionalFilter(@Nonnull final OptionalHandling handling) {
        return new ArtifactFilter() { @Override public boolean include(@Nonnull Artifact artifact) {
            return handling == null || handling == withAndWithoutOptionals || (handling == onlyWithoutOptionals && !artifact.isOptional()) || (handling == onlyWithOptionals && artifact.isOptional());
        }};
    }

    @Nonnull
    protected ArtifactResolutionRequest createRequestFor(@Nonnull MavenProject project, @Nonnull Artifact artifact) {
        final MavenExecutionRequest request = _containerAndRequest.getRequest();
        final ArtifactResolutionRequest result = new ArtifactResolutionRequest();
        result.setArtifact(artifact);
        result.setLocalRepository(getLocalRepository());
        result.setRemoteRepositories(project.getRemoteArtifactRepositories());
        result.setServers(request.getServers());
        result.setOffline(request.isOffline());
        result.setMirrors(request.getMirrors());
        result.setProxies(request.getProxies());
        return result;
    }

    @Nonnull
    protected String getLatestVersionOf(@Nonnull MavenProject project, @Nonnull Artifact artifact) throws ArtifactResolutionException {
        String latestVersion = null;
        try {
            final ArtifactRepositoryMetadata repositoryMetadata = new ArtifactRepositoryMetadata(artifact);
            _repositoryMetadataManager.resolve(repositoryMetadata, project.getRemoteArtifactRepositories(), getLocalRepository());
            latestVersion = selectLatestMatchingVersion(repositoryMetadata, artifact);
        } catch (final Exception ignored) {}

        if (latestVersion == null || latestVersion.isEmpty()) {
            throw new ArtifactResolutionException("Could not find a version in the remote repositories but also no latestVersion in the local repository. Stop resolving now.", artifact);
        }
        return latestVersion;
    }

    @Nonnull
    protected ArtifactRepository getLocalRepository() {
        return _containerAndRequest.getRequest().getLocalRepository();
    }

    @Nullable
    protected String selectLatestMatchingVersion(@Nonnull ArtifactRepositoryMetadata repositoryMetadata, @Nonnull Artifact artifact) {
        final Metadata metadata = repositoryMetadata.getMetadata();
        String latestMatchingVersion = null;
        final Versioning versioning = metadata.getVersioning();
        if (versioning != null) {
            final List<String> versions = versioning.getVersions();
            final ListIterator<String> i = versions.listIterator(versions.size());
            final VersionRange versionRange = artifact.getVersionRange();
            while (i.hasPrevious() && isEmpty(latestMatchingVersion)) {
                final String current = i.previous();
                if (versionRange.containsVersion(new DefaultArtifactVersion(current))) {
                    latestMatchingVersion = current;
                }
            }
        }
        return latestMatchingVersion;
    }

    @Nonnull
    protected Set<ArtifactWithDependencies> getDependencies(@Nonnull MavenProject project, @Nonnull Artifact artifact, @Nonnull ArtifactFactoryRequest request) throws Exception {
        final ArtifactResolutionResult resolutionResult = resolve(project, artifact, request, true);
        final ArtifactFilter filter = toArtifactFilter(request);

        final Set<ArtifactWithDependencies> dependencies = new LinkedHashSet<>();
        for (final Artifact dependency : resolutionResult.getArtifacts()) {
            if (!project.getGroupId().equals(dependency.getGroupId()) || !project.getArtifactId().equals(dependency.getArtifactId())) {
                if (filter.include(dependency)) {
                    resolve(project, dependency, request, false);
                    dependencies.add(toArtifactWithDependencies(project, dependency, request));
                }
            }
        }
        return dependencies;
    }

    @Nonnull
    public ArtifactWithDependencies toArtifactWithDependencies(@Nonnull final MavenProject project, @Nonnull final Artifact artifact, @Nonnull final ArtifactFactoryRequest request) {
        return ArtifactWithDependenciesFactory.create(artifact, new Callable<Set<ArtifactWithDependencies>>() {
            @Override
            public Set<ArtifactWithDependencies> call() {
                try {
                    return getDependencies(project, artifact, request);
                } catch (final Exception e) {
                    throw new RuntimeException("Could not get the dependencies of " + artifact + ".", e);
                }
            }
        });
    }

    @Nonnull
    private ContainerAndRequest createContainerAndRequest() throws Exception {
        final ClassWorld classWorld = new ClassWorld("plexus.core", Thread.currentThread().getContextClassLoader());
        final Class<?> cliRequestClass = getCliRequestClass();
        final Object cliRequest = createCliRequest(classWorld, cliRequestClass);
        final MavenExecutionRequest request = getRequestOf(cliRequest);
        final AtomicReference<PlexusContainer> containerReference = new AtomicReference<>();
        final AtomicReference<DefaultMaven> oldMaven = new AtomicReference<>();
        final DefaultMavenExecutionResult executionResult = new DefaultMavenExecutionResult();
        final Integer result = invokeCli(cliRequestClass, cliRequest, containerReference, oldMaven, executionResult);
        if (!new Integer(0).equals(result)) {
            throw new RuntimeException("Error while initiating the maven context. See message above.");
        }
        final MavenExecutionRequest resultRequest = injectDefaults(request, containerReference, oldMaven, executionResult);
        return new ContainerAndRequest((DefaultPlexusContainer) containerReference.get(), resultRequest);
    }

    @Nonnull
    protected MavenExecutionRequest getRequestOf(@Nonnull Object cliRequest) throws Exception {
        final Field requestField = cliRequest.getClass().getDeclaredField("request");
        requestField.setAccessible(true);
        return (MavenExecutionRequest) requestField.get(cliRequest);
    }

    @Nonnull
    protected Object createCliRequest(@Nonnull ClassWorld classWorld, @Nonnull Class<?> cliRequestClass) throws Exception {
        final Constructor<?> constructor = cliRequestClass.getDeclaredConstructor(String[].class, ClassWorld.class);
        constructor.setAccessible(true);
        return constructor.newInstance(new String[0], classWorld);
    }

    @Nonnull
    protected Class<?> getCliRequestClass() throws ClassNotFoundException {
        return MavenCli.class.getClassLoader().loadClass(MavenCli.class.getName() + "$CliRequest");
    }


    @Nonnull
    protected Integer invokeCli(@Nonnull Class<?> cliRequestClass, @Nonnull Object cliRequest, @Nonnull final AtomicReference<PlexusContainer> containerReference, @Nonnull final AtomicReference<DefaultMaven> oldMaven, @Nonnull final DefaultMavenExecutionResult executionResult) throws Exception {
        final Maven maven = toMaven(executionResult);
        final MavenCli cli = createCli(containerReference, oldMaven, maven);
        return (Integer) MavenCli.class.getMethod("doMain", cliRequestClass).invoke(cli, cliRequest);
    }

    protected MavenCli createCli(final AtomicReference<PlexusContainer> containerReference, final AtomicReference<DefaultMaven> oldMaven, final Maven maven) {
        return new MavenCli() {
            @Override
            protected void customizeContainer(PlexusContainer container) {
                try {
                    oldMaven.set((DefaultMaven)container.lookup(Maven.class));
                } catch (final ComponentLookupException e) {
                    throw new RuntimeException(e);
                }
                container.addComponent(maven, Maven.class, "");
                containerReference.set(container);
            }
        };
    }

    @Nonnull
    protected Maven toMaven(@Nonnull final DefaultMavenExecutionResult executionResult) {
        return new Maven() { @Override public MavenExecutionResult execute(MavenExecutionRequest request) {
            return executionResult;
        }};
    }

    private MavenExecutionRequest injectDefaults(MavenExecutionRequest request, AtomicReference<PlexusContainer> containerReference, AtomicReference<DefaultMaven> oldMaven, DefaultMavenExecutionResult executionResult) throws ComponentLookupException, MavenExecutionRequestPopulationException {
        final MavenExecutionRequestPopulator populator = containerReference.get().lookup(MavenExecutionRequestPopulator.class);
        final MavenExecutionRequest resultRequest = populator.populateDefaults(request);

        final RepositorySystemSession repositorySystemSession = oldMaven.get().newRepositorySession(resultRequest);
        resultRequest.getProjectBuildingRequest().setRepositorySession(repositorySystemSession);

        final MavenSession session = new MavenSession(containerReference.get(), repositorySystemSession, request, executionResult);
        final LegacySupport legacySupport = containerReference.get().lookup(LegacySupport.class);
        legacySupport.setSession(session);
        return resultRequest;
    }

    protected static class ContainerAndRequest {
        private final DefaultPlexusContainer _container;
        private final MavenExecutionRequest _request;

        protected ContainerAndRequest(@Nonnull DefaultPlexusContainer container, @Nonnull MavenExecutionRequest request) {
            _container = container;
            _request = request;
        }

        @Nonnull
        protected DefaultPlexusContainer getContainer() {
            return _container;
        }

        @Nonnull
        protected MavenExecutionRequest getRequest() {
            return _request;
        }
    }

    @Nonnull
    public ArtifactHandlerManager getArtifactHandlerManager() {
        return _artifactHandlerManager;
    }

    public void setTransferListener(@Nonnull TransferListener transferListener) {
        ((DefaultRepositorySystemSession)_containerAndRequest.getRequest().getProjectBuildingRequest().getRepositorySession()).setTransferListener(transferListener);
    }

    public void setLogger(@Nonnull Logger logger) {
        _containerAndRequest.getContainer().setLoggerManager(new MavenLoggerManager(logger));
    }
}
