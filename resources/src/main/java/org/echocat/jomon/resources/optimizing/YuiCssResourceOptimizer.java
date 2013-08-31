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

import org.echocat.jomon.resources.MemoryResourceGenerator;
import org.echocat.jomon.resources.Resource;
import org.echocat.jomon.resources.ResourceGenerator;
import org.echocat.jomon.resources.ResourceType;
import org.echocat.jomon.resources.optimizing.yui.CssCompressorFacade;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.Set;

import static java.util.Collections.singleton;
import static org.echocat.jomon.resources.ResourceType.css;

public class YuiCssResourceOptimizer implements CssResourceOptimizer {

    @Nonnull
    @Override
    public Resource optimize(@Nonnull Resource input, @Nonnull OptimizationContext context) throws Exception {
        final Resource output;
        if (context.isFeatureEnabled(SKIP_CSS_OPTIMIZATION)) {
            output = input;
        } else {
            try (final InputStream inputStream = input.openInputStream()) {
                try (final Reader reader = new InputStreamReader(inputStream)) {
                    try (final ResourceGenerator generator = new MemoryResourceGenerator(input)) {
                        try (final Writer writer = new OutputStreamWriter(generator)) {
                            final CssCompressorFacade compressor = new CssCompressorFacade(reader);
                            compressor.compress(writer, -1);
                        }
                        output = generator.getTemporaryBufferedResource();
                    }
                }
            }
        }
        return output;
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
