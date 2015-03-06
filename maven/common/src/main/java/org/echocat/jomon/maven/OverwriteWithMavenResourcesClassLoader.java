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

import org.apache.commons.collections15.map.LRUMap;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.echocat.jomon.runtime.system.DynamicClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.PriorityOrdered;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static com.google.common.collect.Iterators.*;
import static java.lang.System.setProperty;
import static java.lang.Thread.currentThread;
import static java.util.Collections.synchronizedMap;
import static org.springframework.util.CollectionUtils.isEmpty;

public class OverwriteWithMavenResourcesClassLoader extends ClassLoader implements PriorityOrdered, DynamicClassLoader {

    private static final Logger LOG = LoggerFactory.getLogger(OverwriteWithMavenResourcesClassLoader.class);
    private static final Map<Set<File>, Collection<File>> ROOT_POMS_RESOURCE_ROOT_CACHE = synchronizedMap(new LRUMap<Set<File>, Collection<File>>(10));

    private final Map<String, CacheEntry> _resourceCache = synchronizedMap(new LRUMap<String, CacheEntry>(1000));

    private Collection<File> _rootPoms;
    private boolean _setAsContextLoader;
    private boolean _useJvmWideCaching;

    private volatile Collection<File> _resourceRoots;

    public OverwriteWithMavenResourcesClassLoader(@Nonnull ClassLoader delegate) {
        super(delegate);
    }

    @PostConstruct
    public void init() throws Exception {
        synchronized (this) {
            final Collection<File> rootPoms = _rootPoms;
            if (!isEmpty(rootPoms)) {
                final Set<File> rootPomsAsCacheKey = new HashSet<>(rootPoms);
                _resourceRoots = ROOT_POMS_RESOURCE_ROOT_CACHE.get(rootPomsAsCacheKey);
                if (_resourceRoots == null || !_useJvmWideCaching) {
                    LOG.info("Found root poms " + rootPoms + ". Start to scan for usable resource roots...");
                    final List<MavenProjectWithModules> projects = toProjectsRecursively(rootPoms);
                    _resourceRoots =  toResourceRoots(projects);
                    ROOT_POMS_RESOURCE_ROOT_CACHE.put(rootPomsAsCacheKey, rootPoms);
                    LOG.info("Found root poms " + rootPoms + ". Start to scan for usable resource roots... DONE!");
                }
            } else {
                _resourceRoots = null;
            }
            if (_resourceRoots != null && !_resourceRoots.isEmpty()) {
                setProperty("org.apache.jasper.options.development", "true");
                setProperty("org.apache.jasper.options.checkInterval", "1");
            }
        }
        if (_setAsContextLoader) {
            currentThread().setContextClassLoader(this);
        }
    }

    @Override
    public URL getResource(@Nonnull String name) {
        CacheEntry result;
        synchronized (this) {
            result = _resourceCache.get(name);
        }
        if (result == null) {
            final Collection<File> resourceRoots = _resourceRoots;
            File resultFile = null;
            if (resourceRoots != null) {
                if (resultFile != null && !resultFile.isFile()) {
                    resultFile = null;
                }
                if (resultFile == null) {
                    if (resourceRoots != null) {
                        for (final File resourceRoot : resourceRoots) {
                            final File potentialResource = new File(resourceRoot, name);
                            if (potentialResource.isFile()) {
                                resultFile = potentialResource;
                                break;
                            }
                        }
                    }
                }
            }
            if (resultFile != null) {
                try {
                    result = new CacheEntry(resultFile, resultFile.toURI().toURL());
                } catch (final MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                final URL resource = super.getResource(name);
                result = resource != null ? new CacheEntry(null, resource) : null;
            }
            synchronized (this) {
                _resourceCache.put(name, result);
            }
        }
        return result != null ? result.getUrl() : null;
    }

    @Override
    public Enumeration<URL> getResources(@Nonnull String name) throws IOException {
        final Collection<File> resourceRoots = _resourceRoots;
        final Enumeration<URL> result;
        if (resourceRoots != null) {
            final List<URL> foundStaticUrls = new ArrayList<>();
            for (final File resourceRoot : resourceRoots) {
                final File potentialResource = new File(resourceRoot, name);
                if (potentialResource.isFile()) {
                    foundStaticUrls.add(potentialResource.toURI().toURL());
                }
            }
            final Iterator<URL> combined = concat(foundStaticUrls.iterator(), forEnumeration(super.getResources(name)));
            result = asEnumeration(combined);
        } else {
            result = super.getResources(name);
        }
        return result;
    }

    @Nonnull
    protected List<MavenProjectWithModules> toProjectsRecursively(@Nonnull Collection<File> rootPoms) throws Exception {
        final List<MavenProjectWithModules> projects = new ArrayList<>();
        final MavenEnvironment environment = new MavenEnvironmentFactory().create();
        final MavenProjectWithModulesFactory factory = new MavenProjectWithModulesFactory();
        for (final File rootPom : rootPoms) {
            try {
                final MavenProjectWithModules project = factory.createFor(environment, rootPom);
                enrichWithAllModulesRecursively(projects, project);
            } catch (final ProjectBuildingException e) {
                LOG.warn("Could not build the project list for " + rootPom + ". Will ignore it now but this means that we could not erich the paths with it.", e);
            }
        }
        return projects;
    }

    protected void enrichWithAllModulesRecursively(@Nonnull Collection<MavenProjectWithModules> what, @Nonnull MavenProjectWithModules withProject) throws Exception {
        if (!what.contains(withProject)) {
            what.add(withProject);
            final Collection<MavenProjectWithModules> modules = withProject.getModules();
            if (modules != null) {
                for (final MavenProjectWithModules module : modules) {
                    enrichWithAllModulesRecursively(what, module);
                }
            }
        }
    }

    @Nonnull
    protected List<File> toResourceRoots(@Nonnull List<MavenProjectWithModules> projects) throws Exception {
        final List<File> resourceRoots = new ArrayList<>();
        for (final MavenProjectWithModules project : projects) {
            final MavenProject mavenProject = project.getProject();
            enrichWithResources(resourceRoots, mavenProject);
        }
        return resourceRoots;
    }

    protected void enrichWithResources(@Nonnull Collection<File> what, @Nonnull MavenProject of) throws Exception {
        final List<Resource> resources = of.getResources();
        if (resources != null) {
            for (final Resource resource : resources) {
                final String plainDirectory = resource.getDirectory();
                if (plainDirectory != null) {
                    final File directory = new File(plainDirectory).getCanonicalFile();
                    if (directory.isDirectory() && !what.contains(directory)) {
                        what.add(directory);
                    }
                }
            }
        }
    }

    public boolean isSetAsContextLoader() {
        return _setAsContextLoader;
    }

    public void setSetAsContextLoader(boolean setAsContextLoader) {
        _setAsContextLoader = setAsContextLoader;
    }

    public boolean isUseJvmWideCaching() {
        return _useJvmWideCaching;
    }

    public void setUseJvmWideCaching(boolean useJvmWideCaching) {
        _useJvmWideCaching = useJvmWideCaching;
    }

    public Collection<File> getRootPoms() {
        return _rootPoms;
    }

    public void setRootPoms(Collection<File> rootPoms) {
        _rootPoms = rootPoms;
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

    @Override
    public boolean isDynamic() {
        return _resourceRoots != null && !_resourceRoots.isEmpty();
    }

    protected static class CacheEntry {
        private final File _file;
        private final URL _url;

        public CacheEntry(@Nullable File file, @Nonnull URL url) {
            _file = file;
            _url = url;
        }

        public File getFile() {
            return _file;
        }

        public URL getUrl() {
            return _url;
        }
    }
}
