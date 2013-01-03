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

package org.echocat.jomon.format.mylyn;

import org.echocat.jomon.format.Format;
import org.echocat.jomon.format.FormatProviderSupport;
import org.echocat.jomon.format.Source;
import org.echocat.jomon.format.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

import static com.google.common.collect.Iterators.forEnumeration;
import static java.lang.Thread.currentThread;
import static java.util.Collections.*;
import static org.echocat.jomon.format.BasicFormatProvider.NAME_TO_SOURCE_FORMAT;
import static org.echocat.jomon.format.BasicFormatProvider.NAME_TO_TARGET_FORMAT;

public class MylynAssignmentsSupport<F extends Format> extends FormatProviderSupport {

    private static final Logger LOG = LoggerFactory.getLogger(MylynAssignmentsSupport.class);

    public static final String ASSIGNMENTS_RESOURCE_NAME_PREFIX = "META-INF/assignments/";

    private final Map<F, Class<?>> _formatToType;
    private final Map<String, F> _nameToFormat;

    protected MylynAssignmentsSupport(@Nonnull Class<F> formatType, @Nullable Map<F, Class<?>> formatToType) {
        _formatToType = formatToType != null ? formatToType : Collections.<F, Class<?>>emptyMap();
        _nameToFormat = new LinkedHashMap<>(_formatToType.size(), 1f);
        for (F format : formatToType.keySet()) {
            if ((Source.Format.class.isAssignableFrom(formatType) && NAME_TO_SOURCE_FORMAT.containsKey(format.getName())) || (Target.Format.class.isAssignableFrom(formatType) && NAME_TO_TARGET_FORMAT.containsKey(format.getName()))) {
                // Ignore
            } else {
                _nameToFormat.put(format.getName(), format);
            }
        }
    }

    public MylynAssignmentsSupport(@Nonnull Class<F> formatType, @Nonnull String basicTypeName, @Nullable ClassLoader classLoader) {
        this(formatType, loadFormatToDocumentBuilderType(classLoader, formatType, basicTypeName));
    }

    public MylynAssignmentsSupport(@Nonnull Class<F> formatType, @Nonnull String basicTypeName) {
        this(formatType, basicTypeName, null);
    }

    @Nullable
    public Class<?> findTypeFor(@Nonnull F format) {
        return _formatToType.get(format);
    }

    @Nullable
    protected F findFormatFor(@Nonnull String name) {
        return _nameToFormat.get(name);
    }

    @Nonnull
    public static <F extends Format> Map<F, Class<?>> loadFormatToDocumentBuilderType(@Nullable ClassLoader preferredClassLoader, @Nonnull Class<F> formatType, @Nonnull String basicTypeName) {
        final ClassLoader classLoader = preferredClassLoader != null ? preferredClassLoader : currentThread().getContextClassLoader();
        final Map<F, Class<?>> result;
        final Class<?> basicType = findType(classLoader, basicTypeName);
        if (basicType != null) {
            result = loadFormatToType(classLoader, basicType, formatType);
        } else {
            result = emptyMap();
        }
        return result;
    }

    @Nonnull
    private static <F extends Format> Map<F, Class<?>> loadFormatToType(@Nonnull ClassLoader classLoader, @Nonnull Class<?> basicType, @Nonnull Class<F> formatType) {
        final Map<F, Class<?>> result = new LinkedHashMap<>();
        final Iterator<URL> i = resourcesIteratorFor(classLoader, basicType);
        while (i.hasNext()) {
            final URL resource = i.next();
            final Map<String, String> plainAssignments = readPlainAssignmentsFrom(resource);
            for (Entry<String, String> nameToType : plainAssignments.entrySet()) {
                final String formatName = nameToType.getKey().trim();
                final String typeName = nameToType.getValue().trim();
                if (!formatName.isEmpty() && !typeName.isEmpty()) {
                    final Class<?> type = findType(classLoader, typeName);
                    if (type != null) {
                        if (!basicType.isAssignableFrom(type)) {
                            throw new IllegalStateException("In '" + resource + "' was the class " + typeName + " for format type '" + formatName + "' provided. But this is no instance of " + basicType.getName() + ".");
                        }
                        result.put(createFormatBy(formatType, formatName), type);
                    } else {
                        LOG.info("Could not initialize format type '" + formatName + "' because could not find the class in classpath: " + typeName);
                    }
                }
            }
        }
        return unmodifiableMap(result);
    }

    @Nonnull
    private static <F extends Format> F createFormatBy(@Nonnull Class<F> formatType, @Nonnull String formatName) {
        final F result;
        if (Source.Format.class.equals(formatType)) {
            result = formatType.cast(NAME_TO_SOURCE_FORMAT.containsKey(formatName) ? NAME_TO_SOURCE_FORMAT.get(formatName) : new Source.Format.Impl(formatName));
        } else if (Target.Format.class.equals(formatType)) {
            result = formatType.cast(NAME_TO_TARGET_FORMAT.containsKey(formatName) ? NAME_TO_TARGET_FORMAT.get(formatName) : new Target.Format.Impl(formatName));
        } else {
            throw new IllegalArgumentException("Don't know how to handle: " + formatType.getName());
        }
        return result;
    }

    @Nullable
    public static Class<?> findType(@Nonnull ClassLoader classLoader, @Nonnull String typeName) {
        Class<?> result;
        try {
            result = classLoader.loadClass(typeName);
        } catch (ClassNotFoundException ignored) {
            result = null;
        }
        return result;
    }

    @Nonnull
    private static Iterator<URL> resourcesIteratorFor(@Nonnull ClassLoader classLoader, @Nonnull Class<?> basicType) {
        Iterator<URL> resources;
        try {
            resources = forEnumeration(classLoader.getResources(ASSIGNMENTS_RESOURCE_NAME_PREFIX + basicType.getName()));
        } catch (IOException ignored) {
            resources = emptyIterator();
        }
        return resources;
    }

    @Nonnull
    private static Map<String, String> readPlainAssignmentsFrom(@Nonnull URL resource) {
        final Properties assignments = new Properties();
        try {
            try (final InputStream is = resource.openStream()) {
                try (final Reader reader = new InputStreamReader(is)) {
                    assignments.load(reader);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read " + resource + ".", e);
        }
        //noinspection unchecked,RedundantCast
        return (Map<String, String>) (Object) assignments;
    }
}
