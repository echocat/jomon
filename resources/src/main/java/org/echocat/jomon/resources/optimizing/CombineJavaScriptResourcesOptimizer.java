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

import static java.util.Collections.singleton;
import static org.echocat.jomon.resources.ResourceType.js;
import static org.echocat.jomon.resources.optimizing.OptimizationContextUtils.feature;

public class CombineJavaScriptResourcesOptimizer implements ResourcesOptimizer {

    public static final Feature SKIP_JAVA_SCRIPT_COMBINATION = feature(ResourcesOptimizer.class, "skipJavaScriptCombination");

    @Nonnull
    @Override
    public Collection<Resource> optimize(@Nonnull Collection<Resource> inputs, @Nonnull OptimizationContext context) throws Exception {
        final Collection<Resource> outputs = new ArrayList<>();
        try (final ResourceGenerator generator = new MemoryResourceGenerator(js, "/combined/")) {
            boolean jsWritten = false;
            for (Resource input : inputs) {
                if (js.equals(input.getType()) && !context.isFeatureEnabled(SKIP_JAVA_SCRIPT_COMBINATION)) {
                    jsWritten = true;
                    try (final InputStream inputStream = input.openInputStream()) {
                        try (final Reader reader = new InputStreamReader(inputStream)) {
                            final String content = IOUtils.toString(reader);
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
            if (jsWritten) {
                outputs.add(generator.getTemporaryBufferedResource());
            } else {
                generator.drop();
            }
        }
        return outputs;
    }

    @Override
    public boolean isSupporting(@Nonnull ResourceType type) {
        return js.equals(type);
    }

    @Nonnull
    @Override
    public Set<ResourceType> getSupportedResourceTypes() {
        return singleton(js);
    }
}
