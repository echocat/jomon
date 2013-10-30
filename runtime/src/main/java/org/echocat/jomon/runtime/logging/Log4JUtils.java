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

package org.echocat.jomon.runtime.logging;

import org.apache.log4j.xml.DOMConfigurator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.log4j.LogManager.getLoggerRepository;

public class Log4JUtils {

    static {
        tryInstallJulToSlf4jBridge();
        tryFixMdcInSlf4j();
    }

    protected static void tryInstallJulToSlf4jBridge() {
        try {
            final ClassLoader classLoader = Log4JUtils.class.getClassLoader();
            final Handler handler = (Handler) classLoader.loadClass("org.slf4j.bridge.SLF4JBridgeHandler").newInstance();
            final LogManager logManager = LogManager.getLogManager();
            logManager.reset();
            final Logger logger = logManager.getLogger("");
            for (Handler oldHandlers : logger.getHandlers()) {
                logger.removeHandler(oldHandlers);
            }
            logger.addHandler(handler);
        } catch (Exception ignored) {}
    }

    protected static void tryFixMdcInSlf4j() {
        try {
            final ClassLoader classLoader = Log4JUtils.class.getClassLoader();
            final Class<?> mdc = classLoader.loadClass("org.slf4j.MDC");
            final Class<?> mdcAdapter = classLoader.loadClass("org.slf4j.spi.MDCAdapter");
            final Field mdcAdapterField = mdc.getDeclaredField("mdcAdapter");
            if (mdcAdapterField.getType().equals(mdcAdapter)) {
                mdcAdapterField.setAccessible(true);
                final Object delegate = mdcAdapterField.get(null);
                final Object fixed = classLoader.loadClass("org.echocat.jomon.runtime.FixingSlf4jMDCAdapter").getConstructor(mdcAdapter).newInstance(delegate);
                mdcAdapterField.set(null, fixed);
            }
        } catch (Exception ignored) {}
    }

    public static void configureRuntime(@Nullable URL defaultXmlConfigUrl) {
        final URL xmlConfigUrl = resolveXmlConfigUrl(defaultXmlConfigUrl);
        final InputStream is = openConfig(xmlConfigUrl);
        try {
            configureRuntime(is);
        } catch (Exception e) {
            throw new RuntimeException("Could not configureRuntime log4j with " + xmlConfigUrl + ".", e);
        } finally {
            closeQuietly(is);
        }
    }

    public static void configureRuntime(@Nonnull InputStream log4jXmlConfigAsStream) throws IOException {
        final Reader reader = new InputStreamReader(log4jXmlConfigAsStream);
        try {
            configureRuntime(reader);
        } finally {
            closeQuietly(reader);
        }
    }


    public static void configureRuntime(@Nonnull Reader log4jXmlConfigAsReader) throws IOException {
        final DOMConfigurator domConfigurator = new DOMConfigurator();
        domConfigurator.doConfigure(log4jXmlConfigAsReader, getLoggerRepository());
    }

    @Nonnull
    private static URL resolveXmlConfigUrl(@Nullable URL defaultXmlConfigUrl) {
        final String plainConfigUrl = System.getProperty("log4j.configuration", defaultXmlConfigUrl != null ? defaultXmlConfigUrl.toExternalForm() : null);
        if (plainConfigUrl == null) {
            throw new IllegalArgumentException("The system property 'log4j.configuration' is not set.");
        }
        try {
            return new URL(plainConfigUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("The given xmlConfig '" + plainConfigUrl + "' is no valid url.", e);
        }
    }

    @Nonnull
    private static InputStream openConfig(@Nonnull URL xmlConfigUrl) {
        final InputStream is;
        try {
            is = xmlConfigUrl.openStream();
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not open xmlConfig '" + xmlConfigUrl + "'.", e);
        }
        return is;
    }

}
