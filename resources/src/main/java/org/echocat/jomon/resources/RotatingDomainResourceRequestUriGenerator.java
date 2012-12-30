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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * <h1>Usage</h1>
 *
 * <h2>{@link #setBaseUri(URI) baseUri}</h2>
 * <ul>
 *     <li>Provide an URI with no query and no fragment.</li>
 *     <li>Add <code>$hostNumber$</code> in the URI to create the resulting uri. Example <code>https://resources$hostNumber$.mydomain.com/</code></li>
 *     <li>If set to <code>null</code> (default) this ResourceRequestUriGenerator will simple return the value of the delegate.</li>
 * </ul>
 *
 * <h2>{@link #setHostCount(int) hostCount}</h2>
 * <ul>
 *     <li>Will be used to resolve the <code>$hostNumber$</code> placeholder of {@link #setBaseUri(URI) baseUri}.</li>
 *     <li>Creation will produce a number between <code>1</code> and <code>{@link #setHostCount(int) hostCount}</code>.</li>
 * </ul>
 */
public class RotatingDomainResourceRequestUriGenerator implements ResourceRequestUriGenerator {

    private final ResourceRequestUriGenerator _delegate;

    public RotatingDomainResourceRequestUriGenerator(@Nonnull ResourceRequestUriGenerator delegate) {
        _delegate = delegate;
    }

    private URI _baseUri;
    private int _hostCount = 10;

    public URI getBaseUri() {
        return _baseUri;
    }

    public void setBaseUri(URI baseUri) {
        if (baseUri != null && (!isEmpty(baseUri.getRawFragment()) || !isEmpty(baseUri.getRawQuery()))) {
            throw new IllegalArgumentException("Provided baseUri has a fragment and/or query.");
        }
        _baseUri = baseUri;
    }

    @Nonnegative
    public int getHostCount() {
        return _hostCount;
    }

    public void setHostCount(@Nonnegative int hostCount) {
        _hostCount = hostCount;
    }

    @Nonnull
    @Override
    public String generate(@Nonnull Resource forResource) throws IOException {
        final String original = _delegate.generate(forResource);
        final URI baseUri = _baseUri;
        final String result;
        if (baseUri != null) {
            final String prefix = baseUri.toString().replace("$hostNumber$", hostNumberValueFor(forResource));
            result = prefix + original;
        } else {
            result = original;
        }
        return result;
    }

    @Nonnull
    protected String hostNumberValueFor(@Nonnull Resource resource) throws IOException {
        final int hashCode = Arrays.hashCode(resource.getMd5());
        final int normalizedHashCode  = hashCode >= 0 ? hashCode : hashCode * -1;
        return Integer.toString((normalizedHashCode % _hostCount) + 1);
    }

}
