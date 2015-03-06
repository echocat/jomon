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

import org.apache.maven.DefaultMaven;
import org.apache.maven.Maven;
import org.apache.maven.cli.MavenCli;
import org.apache.maven.execution.*;
import org.apache.maven.plugin.LegacySupport;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.aether.RepositorySystemSession;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

public class MavenEnvironmentFactory {

    @Nonnull
    public MavenEnvironment create() throws Exception {
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
        final MavenExecutionRequest resultRequest = warpExecutionRequest(request, containerReference.get());
        final RepositorySystemSession repositorySystemSession = createRepositorySystemSession(oldMaven.get(), resultRequest);
        injectDefaults(request, containerReference, repositorySystemSession, executionResult);
        return new MavenEnvironment((DefaultPlexusContainer) containerReference.get(), resultRequest, repositorySystemSession);
    }

    @Nonnull
    protected Class<?> getCliRequestClass() throws ClassNotFoundException {
        return MavenCli.class.getClassLoader().loadClass(MavenCli.class.getName() + "$CliRequest");
    }

    @Nonnull
    protected Object createCliRequest(@Nonnull ClassWorld classWorld, @Nonnull Class<?> cliRequestClass) throws Exception {
        final Constructor<?> constructor = cliRequestClass.getDeclaredConstructor(String[].class, ClassWorld.class);
        constructor.setAccessible(true);
        return constructor.newInstance(new String[0], classWorld);
    }

    @Nonnull
    protected MavenExecutionRequest getRequestOf(@Nonnull Object cliRequest) throws Exception {
        final Field requestField = cliRequest.getClass().getDeclaredField("request");
        requestField.setAccessible(true);
        return (MavenExecutionRequest) requestField.get(cliRequest);
    }

    @Nonnull
    protected Integer invokeCli(@Nonnull Class<?> cliRequestClass, @Nonnull Object cliRequest, @Nonnull final AtomicReference<PlexusContainer> containerReference, @Nonnull final AtomicReference<DefaultMaven> oldMaven, @Nonnull final DefaultMavenExecutionResult executionResult) throws Exception {
        final Maven maven = toMaven(executionResult);
        final MavenCli cli = createCli(containerReference, oldMaven, maven);
        return (Integer) MavenCli.class.getMethod("doMain", cliRequestClass).invoke(cli, cliRequest);
    }

    @Nonnull
    protected MavenCli createCli(@Nonnull final AtomicReference<PlexusContainer> containerReference, @Nonnull final AtomicReference<DefaultMaven> oldMaven, final Maven maven) {
        return new MavenCli() {
            @Override
            protected void customizeContainer(PlexusContainer container) {
                try {
                    oldMaven.set((DefaultMaven) container.lookup(Maven.class));
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
        return new Maven() {
            @Override
            public MavenExecutionResult execute(MavenExecutionRequest request) {
                return executionResult;
            }
        };
    }

    @Nonnull
    protected void injectDefaults(@Nonnull MavenExecutionRequest request, @Nonnull AtomicReference<PlexusContainer> containerReference, @Nonnull RepositorySystemSession repositorySystemSession, @Nonnull DefaultMavenExecutionResult executionResult) throws Exception {
        final MavenSession session = new MavenSession(containerReference.get(), repositorySystemSession, request, executionResult);
        final LegacySupport legacySupport = containerReference.get().lookup(LegacySupport.class);
        legacySupport.setSession(session);
    }

    @Nonnull
    protected MavenExecutionRequest warpExecutionRequest(@Nonnull MavenExecutionRequest request, @Nonnull PlexusContainer container) throws ComponentLookupException, MavenExecutionRequestPopulationException {
        final MavenExecutionRequestPopulator populator = container.lookup(MavenExecutionRequestPopulator.class);
        return populator.populateDefaults(request);
    }

    @Nonnull
    protected RepositorySystemSession createRepositorySystemSession(@Nonnull DefaultMaven maven, @Nonnull MavenExecutionRequest resultRequest) {
        final RepositorySystemSession repositorySystemSession = maven.newRepositorySession(resultRequest);
        resultRequest.getProjectBuildingRequest().setRepositorySession(repositorySystemSession);
        return repositorySystemSession;
    }


}
