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

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import static java.lang.reflect.Modifier.*;
import static java.util.Collections.unmodifiableSet;

@SuppressWarnings("ConstantNamingConvention")
public class ResourceType {

    public static final ResourceType bin = new ResourceType("bin", "application/octet-stream");
    public static final ResourceType png = new ResourceType("png", "image/png");
    public static final ResourceType jpeg = new ResourceType("jpeg", "image/jpeg");
    public static final ResourceType gif = new ResourceType("gif", "image/gif");
    public static final ResourceType js = new ResourceType("js", "text/javascript");
    public static final ResourceType svg = new ResourceType("svg", "image/svg+xml");
    public static final ResourceType css = new ResourceType("css", "text/css");
    public static final ResourceType html = new ResourceType("html", "text/html");
    public static final ResourceType xml = new ResourceType("xml", "application/xml");
    public static final ResourceType txt = new ResourceType("txt", "text/plain");

    private static final Set<ResourceType> SYSTEM_TYPES;

    static {
        try {
            final Set<ResourceType> types = new HashSet<>();
            enrichWithValuesFromProperties(types);
            enrichWithValuesFromStaticFields(types);
            SYSTEM_TYPES = unmodifiableSet(types);
        } catch (Exception e) {
            throw new RuntimeException("Could not retrieve default types.", e);
        }
    }

    private static void enrichWithValuesFromProperties(@Nonnull Set<ResourceType> types) throws Exception {
        final Enumeration<URL> urls = ResourceType.class.getClassLoader().getResources("META-INF/mimeTypes.properties");
        while (urls.hasMoreElements()) {
            final URL resourceUrl = urls.nextElement();
            final Properties properties = new Properties();
            try (final InputStream is = resourceUrl.openStream()) {
                properties.load(is);
            }
            for (Entry<Object, Object> keyAndValue : properties.entrySet()) {
                types.add(new ResourceType(keyAndValue.getKey().toString(), keyAndValue.getValue().toString()));
            }
        }
    }

    private static void enrichWithValuesFromStaticFields(@Nonnull Set<ResourceType> types) throws Exception {
        for (Field field : ResourceType.class.getDeclaredFields()) {
            final int modifiers = field.getModifiers();
            if (isStatic(modifiers) && isFinal(modifiers) && isPublic(modifiers) && ResourceType.class.equals(field.getType())) {
                types.add((ResourceType) field.get(null));
            }
        }
    }

    @Nonnull
    public static Set<ResourceType> getSystemTypes() {
        return SYSTEM_TYPES;
    }

    private final String _name;
    private final MimeType _mimeType;

    @Nonnull
    private static MimeType parseMimeType(@Nonnull String rawMimeType) throws IllegalArgumentException {
        try {
            return new MimeType(rawMimeType);
        } catch (MimeTypeParseException e) {
            throw new IllegalArgumentException("Could not parse: " + rawMimeType, e);
        }
    }

    public ResourceType(@Nonnull String name, @Nonnull String rawMimeType) throws IllegalArgumentException  {
        this(name, parseMimeType(rawMimeType));
    }

    public ResourceType(@Nonnull String name, @Nonnull MimeType mimeType) throws IllegalArgumentException {
        if (!mimeType.getParameters().isEmpty()) {
            throw new IllegalArgumentException("The provided mimeType '" + mimeType + "' has parameters.");
        }
        _name = name;
        _mimeType = mimeType;
    }

    @Nonnull
    public String getName() {
        return _name;
    }

    @Nonnull
    public MimeType toMimeType() {
        return createCopyOf(_mimeType);
    }

    @Nonnull
    public String getContentType() {
        return _mimeType.toString();
    }

    protected MimeType createCopyOf(@Nonnull MimeType mimeType) {
        return parseMimeType(mimeType.toString());
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (o == null || !(o instanceof ResourceType)) {
            result = false;
        } else {
            result = _name.equals(((ResourceType)o)._name);
        }
        return result;
    }

    @Override
    public int hashCode() {
        return _name.hashCode();
    }
}
