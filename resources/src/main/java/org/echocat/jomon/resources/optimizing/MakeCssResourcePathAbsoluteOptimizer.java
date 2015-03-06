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

package org.echocat.jomon.resources.optimizing;

import org.apache.commons.io.IOUtils;
import org.echocat.jomon.resources.*;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.singleton;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;
import static org.echocat.jomon.resources.ResourceType.css;
import static org.apache.commons.io.FilenameUtils.normalizeNoEndSeparator;

public class MakeCssResourcePathAbsoluteOptimizer implements ResourcePathOptimizer {

    public static final Pattern PATTERN = compile("url\\s*\\((\\s*['\"]?((?:.*?|\\s*?))['\"]?\\s*)\\)|src\\s*=\\s*['\"]((?:.|\\s)*?)['\"]", CASE_INSENSITIVE);
    public static final Pattern PATH_FIX_PATTERN = compile("//+");

    @Nonnull
    @Override
    public Resource optimize(@Nonnull Resource input, @Nonnull OptimizationContext context) throws Exception {
        final Resource output;
        if (input instanceof UriEnabledResource && !context.isFeatureEnabled(SKIP_PATH_OPTIMIZATION)) {
            try (final InputStream inputStream = input.openInputStream()) {
                try (final Reader reader = new InputStreamReader(inputStream)) {
                    final String plainContent = IOUtils.toString(reader);
                    final String optimizedBody = handleCssBody(plainContent, ((UriEnabledResource) input).getUri(), context);
                    try (final ResourceGenerator generator = new MemoryResourceGenerator(input)) {
                        try (final Writer writer = new OutputStreamWriter(generator)) {
                            writer.write(optimizedBody);
                        }
                        output = generator.getTemporaryBufferedResource(); 
                    }
                }
            }
        } else {
            output = input;
        }
        return output;
    }

    @Nonnull
    protected String handleCssBody(@Nonnull String oldCssBody, @Nonnull String uri, @Nonnull OptimizationContext context) throws Exception {
        final Matcher matcher = PATTERN.matcher(oldCssBody);
        final StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            final String oldMatch = matcher.group();
            final String oldUri = matcher.group(3) != null ? matcher.group(3) : matcher.group(2);
            final String uriContent = matcher.group(1) != null ? matcher.group(1) : oldUri;

            final String newUri = newUri(uri, oldUri, context);
            if (newUri != null && !oldUri.equals(newUri)) {
                final String newReplacement = Matcher.quoteReplacement(oldMatch.replace(uriContent, newUri));
                matcher.appendReplacement(sb, newReplacement);
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    @Nonnull
    protected String newUri(@Nonnull String cssUri, @Nonnull String resourceUri, @Nonnull OptimizationContext context) throws Exception{
        String result;
        try {
            new URL(resourceUri);
            result = resourceUri;
        } catch (final MalformedURLException ignored) {
            if (resourceUri.startsWith("/")) {
                result = resourceUri;
            } else {
                final String plainTargetUri = cssUri + "/../" + resourceUri;
                final String targetUri = PATH_FIX_PATTERN.matcher(plainTargetUri).replaceAll("/");
                result = normalizeNoEndSeparator(targetUri, true);
                if (result == null) {
                    throw new IllegalArgumentException("In " + cssUri + " was an invalid uri for an resource provided: "+ resourceUri);
                }
            }
            result = toOptimizedUri(cssUri, result, context);
        }
        return result;
    }

    private String toOptimizedUri(@Nonnull String cssUri, @Nonnull String uri, @Nonnull OptimizationContext context) throws Exception {
        int indexToTrimAfter = uri.indexOf('?');
        if (indexToTrimAfter < 0) {
            indexToTrimAfter = uri.indexOf('#');
        }
        final String uriPrefix;
        final String uriSuffix;
        if (indexToTrimAfter > 0 && indexToTrimAfter + 1 < uri.length()) {
            uriPrefix = uri.substring(0, indexToTrimAfter);
            uriSuffix = uri.substring(indexToTrimAfter);
        } else {
            uriPrefix = uri;
            uriSuffix = "";
        }
        final String optimizedPrefix = context.findOptimizeAndReturnUriFor(uriPrefix);
        if (optimizedPrefix == null) {
            throw new IllegalArgumentException("Could not find any resource named '" + uri + "' defined in " + cssUri + ".");
        }
        return optimizedPrefix + uriSuffix;
    }

    @Override
    public boolean isSupporting(@Nonnull ResourceType type) {
        return css.equals(type);
    }

    @Nonnull
    @Override
    public Set<ResourceType> getSupportedResourceTypes() {
        return singleton(css);
    }
}
