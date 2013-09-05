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
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.echocat.jomon.maven.boot.ArtifactFactoryRequest.OptionalHandling.*;
import static org.apache.maven.artifact.versioning.VersionRange.createFromVersionSpec;

public abstract class ArtifactFactoryRequest {

    public static enum OptionalHandling {
        onlyWithOptionals,
        onlyWithoutOptionals,
        withAndWithoutOptionals
    }

    private final Set<String> _includeScopes = new HashSet<>();
    private final Set<String> _excludeScopes = new HashSet<>();
    private OptionalHandling _optionalHandling = withAndWithoutOptionals;
    private boolean _forceUpdate;

    protected ArtifactFactoryRequest() {}

    @Nonnull
    public ArtifactFactoryRequest whichIncludesScope(@Nonnull String... scope) {
        return whichIncludesScopes(asList(scope));
    }

    @Nonnull
    public ArtifactFactoryRequest whichIncludesScopes(@Nonnull Collection<String> scopes) {
        _includeScopes.addAll(scopes);
        return this;
    }

    @Nonnull
    public ArtifactFactoryRequest whichExcludesScope(@Nonnull String... scope) {
        return whichExcludesScopes(asList(scope));
    }

    @Nonnull
    public ArtifactFactoryRequest whichExcludesScopes(@Nonnull Collection<String> scopes) {
        _excludeScopes.addAll(scopes);
        return this;
    }

    @Nonnull
    public ArtifactFactoryRequest whichOnlyIncludesRuntimeScopes() {
        _excludeScopes.add("test");
        _excludeScopes.add("provided");
        return this;
    }

    @Nonnull
    public ArtifactFactoryRequest onlyWithoutOptionals() {
        _optionalHandling = onlyWithoutOptionals;
        return this;
    }

    @Nonnull
    public ArtifactFactoryRequest onlyWithOptionals() {
        _optionalHandling = onlyWithOptionals;
        return this;
    }

    @Nonnull
    public ArtifactFactoryRequest withAndWithoutOptionals() {
        _optionalHandling = withAndWithoutOptionals;
        return this;
    }

    @Nonnull
    public ArtifactFactoryRequest withForceUpdates() {
        _forceUpdate = true;
        return this;
    }

    @Nonnull
    public ArtifactFactoryRequest withoutForceUpdates() {
        _forceUpdate = false;
        return this;
    }

    @Nonnull
    public static ArtifactFactoryRequest forArtifact(@Nonnull final Artifact artifact) {
        final ArtifactFactoryRequest result = new ArtifactFactoryRequest() { @Override protected Artifact getArtifact() {
            return artifact;
        }};
        return result;
    }

    @SuppressWarnings("ClassReferencesSubclass")
    @Nonnull
    public static BuildArtifact forArtifact(@Nonnull ArtifactHandlerManager artifactHandlerManager) {
        return new BuildArtifact(artifactHandlerManager);
    }

    @SuppressWarnings("ClassReferencesSubclass")
    @Nonnull
    public static BuildArtifact forArtifact(@Nonnull ArtifactHandlerManager artifactHandlerManager, @Nonnull String artifactIdentifierString) throws InvalidArtifactIdentifierException {
        final BuildArtifact build = forArtifact(artifactHandlerManager);
        final String[] parts = artifactIdentifierString.split(":");
        try {
            if (parts.length == 2) { // groupId:artifactId
                    build.withGroupId(parts[0]).withArtifactId(parts[1]).withVersion(createFromVersionSpec("[0,]"));
            } else if (parts.length == 3) { // groupId:artifactId:version
                build.withGroupId(parts[0]).withArtifactId(parts[1]).withVersion(createFromVersionSpec(parts[2]));
            } else if (parts.length == 4) { // groupId:artifactId:version:classifier
                build.withGroupId(parts[0]).withArtifactId(parts[1]).withVersion(createFromVersionSpec(parts[2])).withClassifier(parts[3]);
            } else {
                throw new InvalidArtifactIdentifierException(artifactIdentifierString);
            }
        } catch (InvalidVersionSpecificationException e) {
            throw new InvalidArtifactIdentifierException(artifactIdentifierString, e);
        }
        return build;
    }
    
    public static class BuildArtifact extends ArtifactFactoryRequest {

        private final ArtifactHandlerManager _artifactHandlerManager;

        private String _groupId;
        private String _artifactId;
        private VersionRange _version;
        private String _type;
        private String _classifier;
        private String _scope;
        private boolean _includesSnapshots;

        private BuildArtifact(@Nonnull ArtifactHandlerManager artifactHandlerManager) {
            _artifactHandlerManager = artifactHandlerManager;
        }

        @Nonnull 
        public BuildArtifact withGroupId(@Nonnull String groupId) {
            _groupId = groupId;
            return this;
        }

        @Nonnull
        public BuildArtifact withArtifactId(@Nonnull String artifactId) {
            _artifactId = artifactId;
            return this;
        }

        @Nonnull
        public BuildArtifact withVersion(@Nonnull VersionRange version) {
            _version = version;
            return this;
        }

        @Nonnull
        public BuildArtifact withType(@Nullable String type) {
            _type = type;
            return this;
        }

        @Nonnull
        public BuildArtifact withClassifier(@Nullable String classifier) {
            _classifier = classifier;
            return this;
        }

        @Nonnull
        public BuildArtifact withScope(@Nullable String scope) {
            _scope = scope;
            return this;
        }

        @Nonnull
        public BuildArtifact whichIncludesSnapshots() {
            _includesSnapshots = true;
            return this;
        }

        @Nonnull @Override public BuildArtifact whichIncludesScope(@Nonnull String... scope) { return (BuildArtifact) super.whichIncludesScope(scope); }
        @Nonnull @Override public BuildArtifact whichIncludesScopes(@Nonnull Collection<String> scopes) { return (BuildArtifact) super.whichIncludesScopes(scopes); }
        @Nonnull @Override public BuildArtifact whichExcludesScope(@Nonnull String... scope) { return (BuildArtifact) super.whichExcludesScope(scope); }
        @Nonnull @Override public BuildArtifact whichExcludesScopes(@Nonnull Collection<String> scopes) { return (BuildArtifact) super.whichExcludesScopes(scopes); }
        @Nonnull @Override public BuildArtifact whichOnlyIncludesRuntimeScopes() { return (BuildArtifact) super.whichOnlyIncludesRuntimeScopes(); }
        @Nonnull @Override public BuildArtifact onlyWithoutOptionals() { return (BuildArtifact) super.onlyWithoutOptionals(); }
        @Nonnull @Override public BuildArtifact onlyWithOptionals() { return (BuildArtifact) super.onlyWithOptionals(); }
        @Nonnull @Override public BuildArtifact withAndWithoutOptionals() { return (BuildArtifact) super.withAndWithoutOptionals(); }
        @Nonnull @Override public BuildArtifact withForceUpdates() { return (BuildArtifact) super.withForceUpdates(); }
        @Nonnull @Override public BuildArtifact withoutForceUpdates() { return (BuildArtifact) super.withoutForceUpdates(); }

        @Override
        protected Artifact getArtifact() {
            final String type = _type != null ? _type : "jar";
            final ArtifactHandler handler = _artifactHandlerManager.getArtifactHandler(type);
            final Artifact artifact = new SnapshotIncludingArtifact(_groupId, _artifactId, _version, _scope, type, _classifier, handler, _includesSnapshots);
            return artifact;
        }
    }
    
    protected abstract Artifact getArtifact();

    protected Set<String> getIncludeScopes() {
        return _includeScopes;
    }

    protected Set<String> getExcludeScopes() {
        return _excludeScopes;
    }

    protected OptionalHandling getOptionalHandling() {
        return _optionalHandling;
    }

    protected boolean isForceUpdate() {
        return _forceUpdate;
    }

    public static class InvalidArtifactIdentifierException extends RuntimeException {

        private InvalidArtifactIdentifierException(String message) {
            super(message);
        }

        private InvalidArtifactIdentifierException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
