/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2014 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.runtime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

import static java.util.Collections.emptyIterator;
import static java.util.jar.Attributes.Name.IMPLEMENTATION_TITLE;
import static java.util.jar.Attributes.Name.IMPLEMENTATION_VERSION;
import static java.util.jar.JarFile.MANIFEST_NAME;
import static org.apache.commons.collections15.IteratorUtils.asIterator;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class ManifestInformationFactory {

    public static final Name IMPLEMENTATION_BUILD_REVISION = new Name("Implementation-Build-Revision");
    public static final Name IMPLEMENTATION_BUILD_DATE = new Name("Implementation-Build-Date");
    public static final Name IMPLEMENTATION_BANNER_FILE = new Name("Implementation-Banner-File");

    @Nonnull
    private final String _resource;
    @Nonnull
    private final ClassLoader _classLoader;

    @Nullable
    private volatile Manifest _manifest;
    @Nullable
    private volatile String _applicationInfoString;

    public ManifestInformationFactory() {
        this(ManifestInformationFactory.class);
    }

    public ManifestInformationFactory(@Nonnull Class<?> baseClass) {
        this(baseClass.getName().replace('.', '/') + ".class", baseClass.getClassLoader());
    }

    public ManifestInformationFactory(@Nonnull String resource, @Nonnull ClassLoader classLoader) {
        _resource = resource;
        _classLoader = classLoader;
    }

    @Nullable
    public String getManifestValue(@Nonnull Name mainAttributes) {
        final Manifest manifest = getManifest();
        return getManifestValue(mainAttributes, manifest);
    }

    @Nullable
    protected String getManifestValue(@Nonnull Name mainAttributes, @Nonnull Manifest of) {
        final String result;
        final Object plainVersion = of.getMainAttributes().get(mainAttributes);
        if (plainVersion != null) {
            result = plainVersion.toString();
        } else {
            result = null;
        }
        return result;
    }

    @Nullable
    public String getImplementationVersion() {
        return getManifestValue(IMPLEMENTATION_VERSION);
    }

    @Nullable
    public String getImplementationTitle() {
        return getManifestValue(IMPLEMENTATION_TITLE);
    }

    @Nullable
    public String getImplementationBuildRevision() {
        return getManifestValue(IMPLEMENTATION_BUILD_REVISION);
    }

    @Nullable
    public String getImplementationBuildDate() {
        return getManifestValue(IMPLEMENTATION_BUILD_DATE);
    }

    @Nullable
    public String getImplementationBannerFile() {
        return getManifestValue(IMPLEMENTATION_BANNER_FILE);
    }

    @Nullable
    public String getApplicationInfoString() {
        if (_applicationInfoString == null) {
            final String title = getImplementationTitle();
            final String version = getImplementationVersion();
            final String buildRevision = getImplementationBuildRevision();
            final String buildDate = getImplementationBuildDate();
            final StringBuilder sb = new StringBuilder();
            if (!isEmpty(title)) {
                sb.append(title);
            }
            if (!isEmpty(version)) {
                if (!isEmpty(title)) {
                    sb.append(' ');
                }
                sb.append(version);
            }
            if (!isEmpty(buildDate) || !isEmpty(buildRevision)) {
                final boolean surround = sb.length() > 0;
                if (surround) {
                    sb.append(" (");
                }
                if (!isEmpty(buildRevision)) {
                    sb.append(buildRevision);
                }
                if (!isEmpty(buildDate)) {
                    if (!isEmpty(buildRevision)) {
                        sb.append(", ");
                    }
                    sb.append(buildDate);
                }
                if (surround) {
                    sb.append(')');
                }
            }
            _applicationInfoString = sb.toString().trim();
        }
        return _applicationInfoString.isEmpty() ? null : _applicationInfoString;
    }

    @Nonnull
    public Manifest getManifest() {
        if (_manifest == null) {
            final URL manifestUrl = findManifestUrl();
            if (manifestUrl != null) {
                try (final InputStream is = manifestUrl.openStream()) {
                    _manifest = new Manifest(is);
                } catch (final IOException e) {
                    throw new RuntimeException("Could not read manifest from " + manifestUrl + ".", e);
                }
            } else {
                _manifest = new Manifest();
            }
        }
        return _manifest;
    }

    @Nullable
    private URL findManifestUrl() {
        final Iterator<URL> i = findResources(_resource);
        if (!i.hasNext()) {
            throw new IllegalStateException("Could not find resource of " + _resource + ".");
        }
        URL manifestUrl = null;
        while (i.hasNext() && manifestUrl == null) {
            final URL classUrl = i.next();
            final String base = findBaseFor(classUrl, _resource);
            if (base != null) {
                manifestUrl = findManifestUrlStartsWith(base);
            }
        }
        return manifestUrl;
    }

    @Nullable
    private String findBaseFor(@Nonnull URL classUrl, @Nonnull String classResourceName) {
        final String classUrlString = classUrl.toString();
        final int cutBefore = classUrlString.lastIndexOf(classResourceName);
        final String base;
        if (cutBefore > 0 && classUrlString.endsWith(classResourceName)) {
            base = classUrlString.substring(0, cutBefore);
        } else {
            base = null;
        }
        return base;
    }

    @Nullable
    private URL findManifestUrlStartsWith(@Nonnull String manifestUrlStartsWith) {
        URL manifestUrl = null;
        final Iterator<URL> j = findResources(MANIFEST_NAME);
        while (j.hasNext() && manifestUrl == null) {
            final URL currentManifestUrl = j.next();
            if (currentManifestUrl.toString().startsWith(manifestUrlStartsWith)) {
                manifestUrl = currentManifestUrl;
            }
        }
        return manifestUrl;
    }

    @Nullable
    protected Iterator<URL> findResources(@Nonnull String name) {
        Iterator<URL> result = null;
        try {
            final Enumeration<URL> enumeration = _classLoader.getResources(name);
            if (enumeration != null) {
                result = asIterator(enumeration);
            }
        } catch (final IOException e) {
            throw new RuntimeException("Could not find all resources of " + name + ".", e);
        }
        if (result == null) {
            result = emptyIterator();
        }
        return result;
    }

    @Nonnull
    public ClassLoader getClassLoader() {
        return _classLoader;
    }

}
