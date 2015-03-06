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
import org.echocat.jomon.resources.MemoryResourceGenerator;
import org.echocat.jomon.resources.Resource;
import org.echocat.jomon.resources.ResourceGenerator;
import org.echocat.jomon.resources.ResourceType;
import org.echocat.jomon.resources.optimizing.OptimizationContext.Feature;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.singleton;
import static java.util.regex.Matcher.quoteReplacement;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;
import static org.echocat.jomon.resources.ResourceType.css;
import static org.echocat.jomon.resources.optimizing.OptimizationContextUtils.feature;

public class CombineCssResourcesOptimizer implements ResourcesOptimizer {

    public static final Pattern IMPORT_PATTERN = compile("\\s*@import (?:url\\s*\\((\\s*['\"]?((?:.*?|\\s*?))['\"]?\\s*)\\)|src\\s*=\\s*['\"]((?:.|\\s)*?)['\"])\\s*;", CASE_INSENSITIVE);
    public static final Feature SKIP_CSS_COMBINATION = feature(ResourcesOptimizer.class, "skipCssCombination");

    @Nonnull
    @Override
    public Collection<Resource> optimize(@Nonnull Collection<Resource> inputs, @Nonnull OptimizationContext context) throws Exception {
        final Collection<Resource> outputs = new ArrayList<>();
        try (final ResourceGenerator generator = new MemoryResourceGenerator(css, "/combined/")) {
            boolean cssWritten = false;
            for (final Resource input : inputs) {
                if (css.equals(input.getType()) && !context.isFeatureEnabled(SKIP_CSS_COMBINATION)) {
                    cssWritten = true;
                    try (final InputStream inputStream = input.openInputStream()) {
                        try (final Reader reader = new InputStreamReader(inputStream)) {
                            final String plainContent = IOUtils.toString(reader);
                            final String content = handleImports(plainContent, context);
                            try (final Writer writer = new OutputStreamWriter(generator)) {
                                writer.write(content);
                                writer.write("\n");
                            }
                        }
                    } finally {
                        input.release();
                    }
                } else {
                    outputs.add(input);
                }
            }
            if (cssWritten) {
                outputs.add(generator.getTemporaryBufferedResource());
            } else {
                generator.drop();
            }
        }
        return outputs;
    }

    @Nonnull
    protected String handleImports(@Nonnull String oldCssBody, @Nonnull OptimizationContext context) throws Exception {
        final Matcher matcher = IMPORT_PATTERN.matcher(oldCssBody);
        final StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            final String uri = matcher.group(3) != null ? matcher.group(3) : matcher.group(2);
            final String importedBody = getBodyOf(uri, context);
            final String newReplacement = quoteReplacement(importedBody);
            matcher.appendReplacement(sb, newReplacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    @Nonnull
    private String getBodyOf(@Nonnull String uri, @Nonnull OptimizationContext context) throws Exception {
        final Resource resource = context.findAndOptimizeFor(uri);
        if (resource == null) {
            throw new IllegalArgumentException("Could not find referenced import: " + uri);
        }
        try (final InputStream is = resource.openInputStream()) {
            try (final Reader reader = new InputStreamReader(is)) {
                return IOUtils.toString(reader);
            }
        }
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
