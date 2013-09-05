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

package org.echocat.jomon.net.http.browsers;

import org.echocat.jomon.cache.Cache;
import org.echocat.jomon.cache.ClearableCache;
import org.echocat.jomon.cache.LimitedCache;
import org.echocat.jomon.cache.LruCache;
import org.echocat.jomon.cache.management.CacheProvider;
import org.echocat.jomon.net.http.browsers.BrowserDefinitionParser.Row;
import org.echocat.jomon.net.http.browsers.BrowserDefinitionParser.Rows;
import org.echocat.jomon.runtime.util.Duration;
import org.echocat.jomon.runtime.util.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.net.URL;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.echocat.jomon.cache.management.DefaultCacheDefinition.lruCache;

/**
 * <p>Return {@link BrowserDefinition} which contains information on browser version and properties.
 * It loads its definitions by invoking {@link #init()} which retrieves its values from {@link BrowserDefinitionParser}.</p>
 *
 * <p>Many thanks to Gary Keith, who is maintaining this list of browser.
 * See <a href="http://browsers.garykeith.com/downloads.asp">Gary Keith's browser capabilities project</a></p>
 */
public class BrowserDefinitionFactory {

    private URL _browserDefinitionFile = BrowserDefinitionParser.DEFAULT_BROWSER_DEFINITION_FILE;

    private final Cache<String, BrowserDefinition> _cache;

    public BrowserDefinitionFactory(@Nonnull CacheProvider cacheProvider) {
        _cache = cacheProvider.provide(getClass(), "definitions", lruCache(String.class, BrowserDefinition.class).withCapacity(10000).withMaximumLifetime("1d"));
    }

    public BrowserDefinitionFactory() {
        _cache = new LruCache<>(String.class, BrowserDefinition.class);
        ((LimitedCache<String, BrowserDefinition>) _cache).setMaximumLifetime(new Duration("1d"));
        ((LimitedCache<String, BrowserDefinition>) _cache).setCapacity(10000L);
    }

    private List<BrowserDefinition> _browserDefinitions;

    public void setBrowserDefinitionFile(@Nonnull URL browserDefinitionFile) {
        _browserDefinitionFile = browserDefinitionFile;
    }

    @PostConstruct
    public void init() throws Exception {
        if (_cache instanceof ClearableCache) {
            ((ClearableCache) _cache).clear();
        }
        try (final Rows rows = new BrowserDefinitionParser(_browserDefinitionFile).iterator()) {
            _browserDefinitions = parseRows(rows);
        }
    }

    @Nonnull
    public BrowserDefinition getByUserAgent(@Nonnull String userAgent) {
        BrowserDefinition browserDefinition = _cache.get(userAgent);
        if (browserDefinition == null) {
            browserDefinition = findFirstMatchInBrowserDefinitions(userAgent);
            _cache.put(userAgent, browserDefinition);
        }
        return browserDefinition;
    }

    @Nonnull
    private List<BrowserDefinition> parseRows(@Nonnull Rows rows) throws Exception {
        final List<BrowserDefinition> browserDefinitions = new ArrayList<>();
        final Map<String, Map<String, String>> parentUserAgentToProperties = new HashMap<>();
        while (rows.hasNext()) {
            final Row row = rows.next();
            final BrowserDefinition browserDefinition = createBrowserDefinitionFrom(row, parentUserAgentToProperties);
            if (browserDefinition.isParent()) {
                parentUserAgentToProperties.put(browserDefinition.getUserAgent(), browserDefinition.getProperties());
            }
            browserDefinitions.add(browserDefinition);
        }
        return browserDefinitions;
    }

    @Nonnull
    private BrowserDefinition createBrowserDefinitionFrom(@Nonnull Row row, @Nonnull Map<String, Map<String, String>> parentUserAgentToProperties) throws Exception {
        final String plainUserAgent = row.getValue("PropertyName");
        if (plainUserAgent == null) {
            throw new IllegalAccessException("The provided " + row + " contains a null UserAgent.");
        }
        final String userAgent = normalizeUserAgent(plainUserAgent);
        final String parentUserAgent = row.getValue("Parent");
        if (parentUserAgent == null) {
            throw new IllegalAccessException("The provided " + row + " contains a null Parent.");
        }
        final Map<String, String> properties = new HashMap<>();
        enrichWithPropertiesOfParentIfPossible(properties, userAgent, parentUserAgent, parentUserAgentToProperties);
        enrichWithPropertiesFromRow(properties, row);
        return new BrowserDefinition(userAgent, properties, parentUserAgent);
    }

    private void enrichWithPropertiesFromRow(@Nonnull Map<String, String> properties, @Nonnull Row row) {
        for (Entry<String, String> entry : row) {
            final String value = entry.getValue();
            if (!"default".equals(value) && !isEmpty(value)) {
                properties.put(entry.getKey(), value);
            }
        }
    }

    private void enrichWithPropertiesOfParentIfPossible(@Nonnull Map<String, String> properties, @Nonnull String actualUserAgent, @Nullable String parentUserAgent, @Nonnull Map<String, Map<String, String>> parentUserAgentToProperties) {
        if (parentUserAgent != null && !parentUserAgent.equals(actualUserAgent)) {
            final Map<String, String> parentUserAgentProperties = parentUserAgentToProperties.get(parentUserAgent);
            if (parentUserAgentProperties != null) {
                properties.putAll(parentUserAgentProperties);
            }
        }
    }

    @Nonnull
    private String normalizeUserAgent(@Nonnull String plainUserAgent) {
        final String userAgent;
        if (plainUserAgent.startsWith("[") && plainUserAgent.endsWith("]")) {
            userAgent = plainUserAgent.substring(1, plainUserAgent.length() - 1);
        } else {
            userAgent = plainUserAgent;
        }
        return userAgent;
    }

    @Nonnull
    private BrowserDefinition findFirstMatchInBrowserDefinitions(@Nonnull String userAgent) {
        BrowserDefinition result = null;
        final Iterator<BrowserDefinition> i = _browserDefinitions.iterator();
        // In case we find a mobile agent this browser definition is the result we look for even if it has no major version.
        // Otherwise we would not support
        while (i.hasNext() && (result == null || (isNotMobileOrTabletDevice(result) && result.getMajorVersion() == null))) {
            final BrowserDefinition browserDefinition = i.next();
            if (browserDefinition.getGlob().matches(userAgent)) {
                if (result == null || !"Default Browser".equals(browserDefinition.getBrowserName())) {
                    result = browserDefinition;
                }
            }
        }
        if (result == null) {
            throw new IllegalStateException("Could not find any browserDefinition for userAgent '" + userAgent + "'?");
        }
        return result;
    }

    private boolean isNotMobileOrTabletDevice(BrowserDefinition result) {
        return !result.isMobileDevice() && !result.isTablet();
    }
}
