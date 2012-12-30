/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012 echocat
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

    private final String _resource;
    private final ClassLoader _classLoader;

    private volatile Manifest _manifest;
    private volatile String _applicationInfoString;

    public ManifestInformationFactory() {
        this(ManifestInformationFactory.class);
    }

    public ManifestInformationFactory(Class<?> baseClass) {
        this(baseClass.getName().replace('.', '/') + ".class", baseClass.getClassLoader());
    }

    public ManifestInformationFactory(String resource, ClassLoader classLoader) {
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
                } catch (IOException e) {
                    throw new RuntimeException("Could not read manifest from " + manifestUrl + ".", e);
                }
            } else {
                _manifest = new Manifest();
            }
        }
        return _manifest;
    }

    private URL findManifestUrl() {
        final Iterator<URL> i = findResources(_resource);
        if (!i.hasNext()) {
            throw new IllegalStateException("Could not find resource of " + _resource + ".");
        }
        URL manifestUrl = null;
        while (i.hasNext() && manifestUrl == null) {
            final URL classUrl = i.next();
            final String base = getBaseFor(classUrl, _resource);
            if (base != null) {
                manifestUrl = findManifestUrlStartsWith(base);
            }
        }
        return manifestUrl;
    }

    private String getBaseFor(URL classUrl, String classResourceName) {
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

    private URL findManifestUrlStartsWith(String manifestUrlStartsWith) {
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

    Iterator<URL> findResources(String name) {
        Iterator<URL> result = null;
        try {
            final Enumeration<URL> enumeration = _classLoader.getResources(name);
            if (enumeration != null) {
                result = asIterator(enumeration);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not find all resources of " + name + ".", e);
        }
        if (result == null) {
            result = emptyIterator();
        }
        return result;
    }

}
