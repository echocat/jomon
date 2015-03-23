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

import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.xml.DOMConfigurator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.log4j.LogManager.getLoggerRepository;

public class Log4JUtils {

    public static void configureRuntime(@Nullable URL defaultXmlConfigUrl) {
        final URL xmlConfigUrl = resolveXmlConfigUrl(defaultXmlConfigUrl);
        final InputStream is = openConfig(xmlConfigUrl);
        try {
            configureRuntime(is);
        } catch (final Exception e) {
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
        configure(log4jXmlConfigAsReader, getLoggerRepository());
    }

    public static void configure(@Nonnull Reader log4jXmlConfigAsReader, @Nonnull LoggerRepository on) throws IOException {
        final DOMConfigurator domConfigurator = new DOMConfigurator();
        domConfigurator.doConfigure(log4jXmlConfigAsReader, on);
    }

    @Nonnull
    private static URL resolveXmlConfigUrl(@Nullable URL defaultXmlConfigUrl) {
        final String plainConfigUrl = System.getProperty("log4j.configuration", defaultXmlConfigUrl != null ? defaultXmlConfigUrl.toExternalForm() : null);
        if (plainConfigUrl == null) {
            throw new IllegalArgumentException("The system property 'log4j.configuration' is not set.");
        }
        try {
            return new URL(plainConfigUrl);
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException("The given xmlConfig '" + plainConfigUrl + "' is no valid url.", e);
        }
    }

    @Nonnull
    private static InputStream openConfig(@Nonnull URL xmlConfigUrl) {
        final InputStream is;
        try {
            is = xmlConfigUrl.openStream();
        } catch (final IOException e) {
            throw new IllegalArgumentException("Could not open xmlConfig '" + xmlConfigUrl + "'.", e);
        }
        return is;
    }

}
