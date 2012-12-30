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

package org.echocat.jomon.resources;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

@ThreadSafe
public class DefaultResourceTypeProvider implements ResourceTypeProvider {

    private final Map<String, ResourceType> _nameToResourceType = new HashMap<>();
    private final Map<String, ResourceType> _contentTypeToResourceType = new HashMap<>();

    public DefaultResourceTypeProvider() {
        this(null);
    }

    public DefaultResourceTypeProvider(@Nullable Set<ResourceType> resourceTypeExtensions) {
        for (ResourceType resourceType : ResourceType.getSystemTypes()) {
            if (!_nameToResourceType.containsKey(resourceType.getName())) {
                _nameToResourceType.put(resourceType.getName(), resourceType);
            }
            if (!_contentTypeToResourceType.containsKey(resourceType.getContentType())) {
                _contentTypeToResourceType.put(resourceType.getContentType(), resourceType);
            }
        }
        if (resourceTypeExtensions != null) {
            for (ResourceType resourceType : resourceTypeExtensions) {
                if (!_nameToResourceType.containsKey(resourceType.getName())) {
                    _nameToResourceType.put(resourceType.getName(), resourceType);
                }
                if (!_contentTypeToResourceType.containsKey(resourceType.getContentType())) {
                    _contentTypeToResourceType.put(resourceType.getContentType(), resourceType);
                }
            }
        }
    }

    @Nonnull
    @Override
    public ResourceType getBy(@Nonnull String name) throws IllegalArgumentException {
        final ResourceType resourceType = _nameToResourceType.get(name);
        if (resourceType == null) {
            throw new IllegalArgumentException("There is no type named: " + name);
        }
        return resourceType;
    }

    @Override
    public ResourceType findByContentType(@Nonnull String contentType) {
        return _contentTypeToResourceType.get(contentType);
    }

    @Override
    public Iterator<ResourceType> iterator() {
        return newHashSet(_nameToResourceType.values()).iterator();
    }
}
