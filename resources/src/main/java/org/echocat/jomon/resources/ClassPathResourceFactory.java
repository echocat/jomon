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

package org.echocat.jomon.resources;

import org.echocat.jomon.runtime.system.DynamicClassLoader;
import org.apache.commons.collections15.map.LRUMap;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.WeakHashMap;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class ClassPathResourceFactory {

    private final Map<ClassLoader, Map<String, ClassPathResource>> _classLoaderToEntriesCache = new WeakHashMap<>();
    private final ResourceTypeProvider _resourceTypeProvider;

    private int _cachedEntriesPerClassLoader = 1000;

    public ClassPathResourceFactory(@Nonnull ResourceTypeProvider resourceTypeProvider) {
        _resourceTypeProvider = resourceTypeProvider;
    }

    @Nonnull
    public ClassPathResource getFor(@Nonnull String path, @Nonnull ClassLoader classLoader) {
        return getFor(path, null, classLoader);
    }

    @Nonnull
    public ClassPathResource getFor(@Nonnull String path, @Nullable ResourceType resourceType, @Nonnull ClassLoader classLoader) {
        final ResourceType originalResourceType = getResourceTypeOf(path);
        ClassPathResource result;
        if (classLoader instanceof DynamicClassLoader && ((DynamicClassLoader)classLoader).isDynamic()) {
            result = new ClassPathResource(path, originalResourceType, classLoader);
        } else {
            final Map<String, ClassPathResource> pathToResource = getPathToResourceCacheFor(classLoader);
            result = pathToResource.get(path);
            // noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (pathToResource) {
                if (result == null) {
                    result = new ClassPathResource(path, originalResourceType, classLoader);
                    pathToResource.put(path, result);
                }
            }
        }
        return resourceType == null || resourceType.equals(originalResourceType) ? result : new OverridingTypeClassPathResource(resourceType, result);
    }

    @Nonnull
    protected ResourceType getResourceTypeOf(@Nonnull String path) {
        final String extension = FilenameUtils.getExtension(path);
        if (isEmpty(extension)) {
            throw new IllegalArgumentException("Found an path without any extension: " + path);
        }
        final ResourceType resourceType;
        try {
            resourceType = _resourceTypeProvider.getBy(extension);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Found an path with an unknown extension: " + path, e);
        }
        return resourceType;
    }

    @Nonnull
    protected Map<String, ClassPathResource> getPathToResourceCacheFor(@Nonnull ClassLoader classLoader) {
        Map<String, ClassPathResource> pathToResource;
        synchronized (_classLoaderToEntriesCache) {
            pathToResource = _classLoaderToEntriesCache.get(classLoader);
            if (pathToResource == null) {
                pathToResource = new LRUMap<>(_cachedEntriesPerClassLoader);
                _classLoaderToEntriesCache.put(classLoader, pathToResource);
            }
        } return pathToResource;
    }

    public int getCachedEntriesPerClassLoader() {
        return _cachedEntriesPerClassLoader;
    }

    public void setCachedEntriesPerClassLoader(int cachedEntriesPerClassLoader) {
        _cachedEntriesPerClassLoader = cachedEntriesPerClassLoader;
    }

    protected static class OverridingTypeClassPathResource extends ClassPathResource {

        @Nonnull
        private final ClassPathResource _original;

        public OverridingTypeClassPathResource(@Nonnull ResourceType overridingType, @Nonnull ClassPathResource original) {
            super(original.getUri(), overridingType, original.getClassLoader());
            _original = original;
        }

        @Nonnull
        @Override
        public InputStream openInputStream() throws IOException {
            return _original.openInputStream();
        }

        @Nonnull
        @Override
        public byte[] getMd5() throws IOException {
            return _original.getMd5();
        }

        @Override
        public long getSize() throws IOException {
            return _original.getSize();
        }

        @Override
        public Date getLastModified() throws IOException {
            return _original.getLastModified();
        }

        @Override
        public boolean isExisting() throws IOException {
            return _original.isExisting();
        }

        @Nonnull
        @Override
        public String getUri() {
            return _original.getUri();
        }

        @Override
        public boolean isGenerated() throws IOException {
            return _original.isGenerated();
        }

        @Nonnull
        @Override
        public URL getPrivateUrl() throws IOException {
            return _original.getPrivateUrl();
        }

        @Nonnull
        @Override
        public String getName() {
            return _original.getName();
        }
    }

}
