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

package org.echocat.jomon.resources.optimizing;

import org.echocat.jomon.resources.MemoryResourceGenerator;
import org.echocat.jomon.resources.Resource;
import org.echocat.jomon.resources.ResourceGenerator;
import org.echocat.jomon.resources.ResourceType;
import org.echocat.jomon.resources.optimizing.yui.JavaScriptCompressorFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.Set;

import static java.util.Collections.singleton;
import static org.apache.commons.io.IOUtils.copy;
import static org.echocat.jomon.resources.ResourceType.js;

public class YuiJavaScriptResourceOptimizer implements JavaScriptResourceOptimizer {

    private static final Logger LOG = LoggerFactory.getLogger(YuiJavaScriptResourceOptimizer.class);
    private static volatile boolean c_verifyErrorLogged;

    private int _lineBreak = 8000;
    private boolean _munge;
    private boolean _verbose;
    private boolean _preserveAllSemiColons;
    private boolean _enableOptimizations = true;

    public int getLineBreak() {
        return _lineBreak;
    }

    public void setLineBreak(int lineBreak) {
        _lineBreak = lineBreak;
    }

    public boolean isMunge() {
        return _munge;
    }

    public void setMunge(boolean munge) {
        _munge = munge;
    }

    public boolean isVerbose() {
        return _verbose;
    }

    public void setVerbose(boolean verbose) {
        _verbose = verbose;
    }

    public boolean isPreserveAllSemiColons() {
        return _preserveAllSemiColons;
    }

    public void setPreserveAllSemiColons(boolean preserveAllSemiColons) {
        _preserveAllSemiColons = preserveAllSemiColons;
    }

    public boolean isEnableOptimizations() {
        return _enableOptimizations;
    }

    public void setEnableOptimizations(boolean enableOptimizations) {
        _enableOptimizations = enableOptimizations;
    }

    @Nonnull
    @Override
    public Resource optimize(@Nonnull Resource input, @Nonnull OptimizationContext context) throws Exception {
        final Resource output;
        if (context.isFeatureEnabled(SKIP_JAVA_SCRIPT_OPTIMIZATION)) {
            output = input;
        } else {
            try (final InputStream inputStream = input.openInputStream()) {
                try (final Reader reader = new InputStreamReader(inputStream)) {
                    try (final ResourceGenerator generator = new MemoryResourceGenerator(input)) {
                        try (final Writer writer = new OutputStreamWriter(generator)) {
                            try {
                                final JavaScriptCompressorFacade compressor = new JavaScriptCompressorFacade(reader);
                                compressor.compress(writer, _lineBreak, _munge, _verbose, _preserveAllSemiColons, !_enableOptimizations);
                            } catch (final VerifyError e) {
                                if (!c_verifyErrorLogged) {
                                    LOG.warn("Could not optimize the sources because got an verifyError while calling the compressor. This message will not appear anymore while running this JVM. The JVM will now not optimize the resources.", e);
                                    c_verifyErrorLogged = true;
                                }
                                copy(reader, writer);
                            }
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
        return js.equals(type);
    }

    @Nonnull
    @Override
    public Set<ResourceType> getSupportedResourceTypes() {
        return singleton(js);
    }

}
