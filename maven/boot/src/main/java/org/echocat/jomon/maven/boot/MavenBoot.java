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

import org.echocat.jomon.maven.boot.ArtifactFactoryRequest.BuildArtifact;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.Arrays.copyOfRange;
import static org.echocat.jomon.maven.boot.ArtifactFactoryRequest.forArtifact;
import static org.echocat.jomon.runtime.Log4JUtils.configureRuntime;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class MavenBoot {

    public static final String ARTIFACT_IDENTIFIER_PROPERTY_NAME = "mavenboot.artifactIdentifier";
    public static final String MAIN_CLASS_PROPERTY_NAME = "mavenboot.mainClass";

    private static final Pattern PROPERTY_EXTRACT_PATTERN = Pattern.compile("-D([^=]+)=(.*)");

    public static void main(String[] arguments) throws Exception {
        configureRuntime(MavenBoot.class.getResource("log4j.xml"));

        final ArtifactFactory factory = new ArtifactFactory();
        final String[] argumentsAfterSystemPropertiesClean = extractDefinitionsOfSystemPropertiesAndSetIt(arguments);
        final RequestWithLeftArguments requestWithLeftArguments = extractRequest(argumentsAfterSystemPropertiesClean, factory.getArtifactHandlerManager());

        final ArtifactWithDependencies artifact = factory.get(requestWithLeftArguments.artifactFactoryRequest
            .whichIncludesSnapshots()
            .whichExcludesScope("test", "provided", "system", "import")
            .onlyWithoutOptionals());
        final ArtifactBasedClassLoader classLoader = new ArtifactBasedClassLoader(MavenBoot.class.getClassLoader(), artifact);

        final Class<?> mainClass = extractMainClass(artifact, classLoader);
        Thread.currentThread().setContextClassLoader(classLoader);
        new MainMethodBooter().execute(mainClass, requestWithLeftArguments.arguments);
    }

    @Nonnull
    private static String[] extractDefinitionsOfSystemPropertiesAndSetIt(@Nonnull String[] input) {
        final ArrayList<String> arguments = new ArrayList<>(asList(input));
        final Iterator<String> i = arguments.iterator();
        boolean thereCouldBeMoreProperties = true;
        while (i.hasNext() && thereCouldBeMoreProperties) {
            final String argument = i.next();
            final Matcher matcher = PROPERTY_EXTRACT_PATTERN.matcher(argument);
            if (matcher.matches()) {
                System.setProperty(matcher.group(1), matcher.group(2));
                i.remove();
            } else {
                thereCouldBeMoreProperties = false;
            }
        }
        return arguments.toArray(new String[arguments.size()]);
    }

    @Nonnull
    private static RequestWithLeftArguments extractRequest(@Nonnull String[] argumentsAfterSystemPropertiesClean, @Nonnull ArtifactHandlerManager artifactHandlerManager) {
        final String artifactIdentifierString = System.getProperty(ARTIFACT_IDENTIFIER_PROPERTY_NAME);
        final String[] arguments;
        final ArtifactFactoryRequest.BuildArtifact artifactFactoryRequest;
        if (!isEmpty(artifactIdentifierString)) {
            artifactFactoryRequest = forArtifact(artifactHandlerManager, artifactIdentifierString);
            arguments = argumentsAfterSystemPropertiesClean;
        } else {
            if (argumentsAfterSystemPropertiesClean.length > 0) {
                artifactFactoryRequest = forArtifact(artifactHandlerManager, argumentsAfterSystemPropertiesClean[0]);
                arguments = copyOfRange(argumentsAfterSystemPropertiesClean, 1, argumentsAfterSystemPropertiesClean.length);
            } else {
                throw new IllegalArgumentException("There was neither the system property '" + ARTIFACT_IDENTIFIER_PROPERTY_NAME + "' set nor the first argument provided.");
            }
        }
        return new RequestWithLeftArguments(artifactFactoryRequest, arguments);
    }

    @Nonnull
    private static Class<?> extractMainClass(@Nonnull ArtifactWithDependencies artifact, @Nonnull ArtifactBasedClassLoader classLoader) throws Exception {
        final String mainClassName = System.getProperty(MAIN_CLASS_PROPERTY_NAME);
        final Class<?> result;
        if (mainClassName != null) {
            try {
                result = classLoader.loadClass(mainClassName);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Could not find the specified main class: " + mainClassName, e);
            }
        } else {
            final ArtifactManifest manifest = new ArtifactManifestFactory().getFor(artifact);
            result = manifest.getMainClass(classLoader);
            if (result == null) {
                throw new RuntimeException("There was neither the system property '" + MAIN_CLASS_PROPERTY_NAME + "' defined nor in the manifest of " + artifact + " was a main class specified.");
            }
        }
        return result;
    }

    private MavenBoot() {
    }

    @SuppressWarnings({"ParameterHidesMemberVariable", "UnusedDeclaration", "InstanceVariableNamingConvention"})
    private static class RequestWithLeftArguments {

        private final ArtifactFactoryRequest.BuildArtifact artifactFactoryRequest;
        private final String[] arguments;

        private RequestWithLeftArguments(@Nonnull BuildArtifact artifactFactoryRequest, @Nonnull String[] arguments) {
            this.artifactFactoryRequest = artifactFactoryRequest;
            this.arguments = arguments;
        }
    }
}
