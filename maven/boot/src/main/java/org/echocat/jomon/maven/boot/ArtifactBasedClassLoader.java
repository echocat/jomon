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
import org.apache.maven.artifact.resolver.ArtifactResolutionException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import static com.google.common.collect.Iterators.asEnumeration;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

public class ArtifactBasedClassLoader extends URLClassLoader {

    private static final URL[] EMPTY_URLS = new URL[0];

    private final Set<Artifact> _artifacts;
    private final ClassLoader _parent;

    public ArtifactBasedClassLoader(@Nullable ClassLoader parentClassLoader, @Nullable ArtifactWithDependencies... artifacts) throws Exception {
        super(EMPTY_URLS, null);
        _artifacts = artifacts != null ? initArtifacts(artifacts) : Collections.<Artifact>emptySet();
        _parent = parentClassLoader != null ? parentClassLoader : Thread.currentThread().getContextClassLoader();
    }

    @Nonnull
    private Set<Artifact> initArtifacts(@Nonnull ArtifactWithDependencies... artifacts) throws Exception {
        final Set<Artifact> result = initArtifacts(asList(artifacts));
        for (Artifact artifact : result) {
            try {
                addURL(artifact.getFile().toURI().toURL());
            } catch (Exception e) {
                throw new ArtifactResolutionException("Could not create file url for artifact.", artifact, e);
            }
        }
        return result;
    }

    @Nonnull
    private Set<Artifact> initArtifacts(@Nonnull Iterable<ArtifactWithDependencies> artifacts) throws Exception {
        final Set<Artifact> addedArtifacts = new HashSet<>();
        initArtifacts(artifacts, addedArtifacts);
        return addedArtifacts;
    }

    private void initArtifacts(@Nonnull Iterable<ArtifactWithDependencies> artifacts, @Nonnull Set<Artifact> alreadyUsedArtifacts) throws Exception {
        for (ArtifactWithDependencies artifact : artifacts) {
            alreadyUsedArtifacts.add(artifact);
            alreadyUsedArtifacts.addAll(artifact.getDependencies());
        }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> result;
        try {
            result = super.loadClass(name);
        } catch (ClassNotFoundException ignored) {
            result = _parent.loadClass(name);
        }
        return result;
    }

    @Override
    public URL getResource(String name) {
        URL result = super.findResource(name);
        if (result == null) {
            result = _parent.getResource(name);
        }
        return result;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        final Set<URL> result = new LinkedHashSet<>();
        final Enumeration<URL> er = super.getResources(name);
        while (er.hasMoreElements()) {
            result.add(er.nextElement());
        }
        final Enumeration<URL> pr = _parent.getResources(name);
        while (pr.hasMoreElements()) {
            result.add(pr.nextElement());
        }
        return asEnumeration(result.iterator());
    }

    @Nonnull
    public Set<Artifact> getArtifacts() {
        return unmodifiableSet(_artifacts);
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (o == null || getClass() != o.getClass()) {
            result = false;
        } else {
            final ArtifactBasedClassLoader that = (ArtifactBasedClassLoader) o;
            result = _artifacts.equals(that.getArtifacts());
        }
        return result;
    }

    @Override
    public int hashCode() {
        return _artifacts.hashCode();
    }

    @Override
    public String toString() {
        return _artifacts.toString();
    }
    
    protected static class ArtifactContainer {
        
        private final ArtifactWithDependencies _artifact;

        protected ArtifactContainer(@Nonnull ArtifactWithDependencies artifact) {
            _artifact = artifact;
        }

        @Nonnull
        protected ArtifactWithDependencies getArtifact() {
            return _artifact;
        }

        @Override
        public boolean equals(Object o) {
            final boolean result;
            if (this == o) {
                result = true;
            } else if (o == null || getClass() != o.getClass()) {
                result = false;
            } else {
                final Artifact that = ((ArtifactContainer) o).getArtifact();
                if (_artifact.getGroupId() != null ? _artifact.getGroupId().equals(that.getGroupId()) : that.getGroupId() == null) {
                    if (_artifact.getArtifactId() != null ? _artifact.getArtifactId().equals(that.getArtifactId()) : that.getArtifactId() == null) {
                        if (_artifact.getType() != null ? _artifact.getType().equals(that.getType()) : that.getType() == null) {
                            result = _artifact.getClassifier() != null ? _artifact.getClassifier().equals(that.getClassifier()) : that.getClassifier() == null;
                        } else {
                            result = false;
                        }
                    } else {
                        result = false;
                    }
                } else {
                    result = false;
                }
            }
            return result;
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 37 * result + (_artifact.getGroupId() != null ? _artifact.getGroupId().hashCode() : 0);
            result = 37 * result + (_artifact.getArtifactId() != null ? _artifact.getArtifactId().hashCode() : 0);
            result = 37 * result + (_artifact.getType() != null ? _artifact.getType().hashCode() : 0);
            result = 37 * result + (_artifact.getClassifier() != null ? _artifact.getClassifier().hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return _artifact.toString();
        }

        public boolean isNewerAs(@Nullable ArtifactContainer other) {
            return other == null || (_artifact.getVersion() != null && _artifact.getVersion().compareTo(other.getArtifact().getVersion()) > 0);
        }
    } 
}
