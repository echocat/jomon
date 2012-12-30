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

package org.echocat.jomon.net.http.browsers;

import org.echocat.jomon.cache.LruCache;
import org.echocat.jomon.cache.management.CacheDefinition;
import org.echocat.jomon.cache.management.CacheProvider;
import org.echocat.jomon.testing.environments.LogEnvironment;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hamcrest.Description;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class BrowserDefinitionFactoryUnitTest {

    private static final Logger LOG = LoggerFactory.getLogger(BrowserDefinitionFactoryUnitTest.class);

    @Rule
    public final LogEnvironment _logEnvironment = new LogEnvironment();

    @Test
    public void test() throws Exception {
        final BrowserDefinitionFactoryImpl factory = new BrowserDefinitionFactoryImpl(cacheProvider());
        factory.init();
        assertThat(factory.getByUserAgent("Generic Mobile Phone (compatible; Googlebot-Mobile/2.1;  http://www.google.com/bot.html)"), isBrowserDefinition().withBrowserName("Googlebot-Mobile").whichIsAMobileDevice().whichIsACrawler().whichIsNotATablet());
        assertThat(factory.getByUserAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3"), isBrowserDefinition().withDeviceName("iPhone").withPlatform("iOS").withPlatformVersion("5.0").whichIsAMobileDevice().whichIsNotATablet().whichIsNotACrawler());
        assertThat(factory.getByUserAgent("Mozilla/5.0 (iPad; CPU OS 5_1 like Mac OS X) AppleWebkit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B176 Safari/7534.48.3"), isBrowserDefinition().withDeviceName("iPad").withPlatform("iOS").withPlatformVersion("5.1").whichIsATablet().whichIsAMobileDevice().whichIsNotACrawler());
        assertThat(factory.getByUserAgent("Mozilla/4.0 (compatible; MSIE 5.0; Windows 98; DigExt)"), isBrowserDefinition().withBrowserName("IE").withPlatform("Win98").withVersion("5.0"));
        assertThat(factory.getByUserAgent("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; Q312461; QXW0339m)"), isBrowserDefinition().withBrowserName("IE").withVersion("6.0").withDeviceName("PC"));
        assertThat(factory.getByUserAgent("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)"), isBrowserDefinition().withBrowserName("IE").withVersion("6.0").withDeviceName("PC"));
        assertThat(factory.getByUserAgent("Mozilla/4.0 (compatible; MSIE 5.5; AOL 7.0; Windows 98; Win 9x 4.90)"), isBrowserDefinition().withBrowserName("IE").withPlatform("Win98").withDeviceName("PC"));
        assertThat(factory.getByUserAgent("Mozilla/5.0 (Windows; U; Win98; de-DE; rv:0.9.4) Gecko/20011019 Netscape6/6.2"), isBrowserDefinition().withBrowserName("Netscape").withPlatform("Win98").withVersion("6.0"));
        assertThat(factory.getByUserAgent("Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.8.0.5) Gecko/20060719 Firefox/1.5.0.5"), isBrowserDefinition().withBrowserName("Firefox").withPlatform("WinXP").withVersion("1.0"));
        assertThat(factory.getByUserAgent("Mozilla/5.0 (Linux; U; Android 4.0.3; de-ch; HTC Sensation Build/IML74K) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30"), isBrowserDefinition().withBrowserName("Android").
                                                                                                                    withDeviceName("Android").withPlatform("Android").withPlatformVersion("4.0").whichIsAMobileDevice().whichIsNotATablet());
        assertThat(factory.getByUserAgent("Mozilla/5.0 (Linux; U; Android 4.0.4; de-de; GT-N7000 Build/IMM76D) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30"), isBrowserDefinition().withBrowserName("Android").
                                                                                                                    withDeviceName("Android").withPlatform("Android").withPlatformVersion("4.0").whichIsAMobileDevice().whichIsNotATablet());
        assertThat(factory.getByUserAgent("Mozilla/5.0 (Linux; U; Android 2.3.5; zh-cn; HTC_IncredibleS_S710e Build/GRJ90) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1"), isBrowserDefinition().withBrowserName("Android").
                                                                                                            withDeviceName("Android").withPlatform("Android").withPlatformVersion("2.3").whichIsAMobileDevice().whichIsNotATablet());
        assertThat(factory.getByUserAgent("Mozilla/5.0 (Linux; U; Android 2.3.4; fr-fr; HTC Desire Build/GRJ22) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1"), isBrowserDefinition().withBrowserName("Android").
                                                                                                            withDeviceName("Android").withPlatform("Android").withPlatformVersion("2.3").whichIsAMobileDevice().whichIsNotATablet());
        assertThat(factory.getByUserAgent("Mozilla/5.0 (Linux; U; Android 2.3.3; zh-tw; HTC_Pyramid Build/GRI40) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1"), isBrowserDefinition().withBrowserName("Android").
                                                                                                            withDeviceName("Android").withPlatform("Android").withPlatformVersion("2.3").whichIsAMobileDevice().whichIsNotATablet());
        assertThat(factory.getByUserAgent("Mozilla/5.0 (Linux; U; Android 2.2; fr-lu; HTC Legend Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1"), isBrowserDefinition().withBrowserName("Android").
                                                                                                            withDeviceName("Android").withPlatform("Android").withPlatformVersion("2.2").whichIsAMobileDevice().whichIsNotATablet());
        assertThat(factory.getByUserAgent("Mozilla/5.0 (Linux; U; Android 1.6; ar-us; SonyEricssonX10i Build/R2BA026) AppleWebKit/528.5+ (KHTML, like Gecko) Version/3.1.2 Mobile Safari/525.20.1"), isBrowserDefinition().withBrowserName("Android").
                                                                                                            withDeviceName("Android").withPlatform("Android").withPlatformVersion("1.6").whichIsAMobileDevice().whichIsNotATablet());
        assertThat(factory.getByUserAgent("Mozilla/5.0 (Android; Mobile; rv:14.0) Gecko/14.0 Firefox/14.0"), isBrowserDefinition().withBrowserName("Firefox").withDeviceName("Android").withPlatform("Android").whichIsAMobileDevice().whichIsNotATablet());
        assertThat(factory.getByUserAgent("Mozilla/5.0 (Android; Mobile; rv:15.0) Gecko/15.0 Firefox/15.0.1"), isBrowserDefinition().withBrowserName("Firefox").withDeviceName("Android").withPlatform("Android").whichIsAMobileDevice().whichIsNotATablet());
        assertThat(factory.getByUserAgent("Opera/9.80 (Android 2.3.5; Linux; Opera Mobi/ADR-1205181138; U; de) Presto/2.10.254 Version/12.00"), isBrowserDefinition().withBrowserName("Opera Mobi").
                                                                                                            withDeviceName("Android").withPlatform("Android").whichIsAMobileDevice().
                                                                                                            whichIsNotATablet()); // ... important test as we expect Opera Mobi as result and not the desktop version
        assertThat(factory.getByUserAgent("Mozilla/5.0 (compatible; MSIE 9.0; Windows Phone OS 7.5; Trident/5.0; IEMobile/9.0)"), isBrowserDefinition().withBrowserName("IEMobile").
                                                                                                            withPlatform("WinPhone7").withVersion("9.0").whichIsAMobileDevice().whichIsNotATablet());
        assertThat(factory.getByUserAgent("Mozilla/4.0 (compatible; MSIE 7.0; Windows Phone OS 7.0; Trident/3.1; IEMobile/7.0; Nokia;N70)"), isBrowserDefinition().withBrowserName("IEMobile").
                                                                                                            withPlatform("WinPhone7").withVersion("7.0").whichIsAMobileDevice().whichIsNotATablet());
        assertThat(factory.getByUserAgent("Opera/9.80 (Windows Mobile; WCE; Opera Mobi/WMD-50433; U; de) Presto/2.4.13 Version/10.00"), isBrowserDefinition().withBrowserName("Opera Mobi").
                withPlatform("WinCE").whichIsAMobileDevice().whichIsNotATablet());


        assertThat(factory.getByUserAgent("Mozilla/5.0 (Windows NT 6.2; rv:12.0) Gecko/20100101 Firefox/12.0"), isBrowserDefinition().withBrowserName("Firefox").withVersion("12.0").withPlatform("Win8").
                                                                                                                withPlatformVersion("6.2").withDeviceName("PC").whichIsNotATablet().whichIsNotAMobileDevice());

        assertThat(factory.getByUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:13.0) Gecko/20100101 Firefox/13.0.1"), isBrowserDefinition().withBrowserName("Firefox").withVersion("13.0").withPlatform("Win7").
                                                                                                                withPlatformVersion("6.1").withDeviceName("PC").whichIsNotATablet().whichIsNotAMobileDevice());

        assertThat(factory.getByUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_5_8) AppleWebKit/534.50.2 (KHTML, like Gecko) Version/5.0.6 Safari/533.22.3"), isBrowserDefinition().withBrowserName("Safari").withVersion("5.0").withPlatform("MacOSX").
                                                                                                                whichIsNotATablet().whichIsNotAMobileDevice());

        assertThat(factory.getByUserAgent("Mozilla/5.0 (X11; U; Linux i686; de; rv:1.9.1.16) Gecko/20120602 Iceweasel/3.5.16 (like Firefox/3.5.16)"), isBrowserDefinition().withBrowserName("Firefox").withVersion("3.0").withPlatform("Linux").
                                                                                                                whichIsNotATablet().whichIsNotAMobileDevice());

        assertThat(factory.getByUserAgent("This must be the default browser"), isBrowserDefinition().withBrowserName("Default Browser"));

        LOG.info(BrowserDefinitionFactory.class.getSimpleName() + ".getByUserAgent() was called " + factory.getCountOfCallsOfGetUserAgent() + " times. This takes " + factory.getTotalMilliSecondsOfCallsOfGetUserAgent() + "ms. The average was " + factory.getAverageMilliSecondsOfCallsOfGetUserAgent() + "ms/call.");
    }

    private CacheProvider cacheProvider() {
        final LruCache<String, BrowserDefinition> cache = new LruCache<>(String.class, BrowserDefinition.class);
        cache.setCapacity(1000L);
        final CacheProvider provider = mock(CacheProvider.class);
        //noinspection unchecked
        doReturn(cache).when(provider).provide(any(Class.class), anyString(), any(CacheDefinition.class));
        return provider;
    }

    private BrowserDefinitionMatcher isBrowserDefinition() {
        return new BrowserDefinitionMatcher();
    }

    private static class BrowserDefinitionFactoryImpl extends BrowserDefinitionFactory {

        private final AtomicInteger _countOfCallsOfGetUserAgent = new AtomicInteger();
        private final AtomicLong _totalMilliSecondsOfCallsOfGetUserAgent = new AtomicLong();

        private BrowserDefinitionFactoryImpl(@Nonnull CacheProvider cacheProvider) {
            super(cacheProvider);
        }

        @Nonnull
        @Override
        public BrowserDefinition getByUserAgent(@Nonnull String userAgent) {
            final long startTimeInMillis = System.currentTimeMillis();
            try {
                return super.getByUserAgent(userAgent);
            } finally {
                _totalMilliSecondsOfCallsOfGetUserAgent.addAndGet(System.currentTimeMillis() - startTimeInMillis);
                _countOfCallsOfGetUserAgent.incrementAndGet();
            }
        }
        
        @Nonnegative
        private long getTotalMilliSecondsOfCallsOfGetUserAgent() {
            return _totalMilliSecondsOfCallsOfGetUserAgent.get();
        }

        @Nonnegative
        private int getCountOfCallsOfGetUserAgent() {
            return _countOfCallsOfGetUserAgent.get();
        }

        @Nonnegative
        private long getAverageMilliSecondsOfCallsOfGetUserAgent() {
            return _totalMilliSecondsOfCallsOfGetUserAgent.get() / _countOfCallsOfGetUserAgent.get();
        }
    }

    private static class BrowserDefinitionMatcher extends TypeSafeMatcher<BrowserDefinition> {

        private String _expectedBrowserName;
        private String _expectedDeviceName;
        private String _expectedVersion;
        private String _expectedPlatform;
        private String _expectedPlatformVersion;
        private Boolean _expectedIsCrawler;
        private Boolean _expectedIsMobile;
        private Boolean _expectedIsTablet;

        @Override
        public boolean matchesSafely(BrowserDefinition browserDefinition) {
            boolean result = false;
            if (_expectedBrowserName == null || _expectedBrowserName.equals(browserDefinition.getBrowserName())) {
                if (_expectedDeviceName == null || _expectedDeviceName.equals(browserDefinition.getDeviceName())) {
                    if (_expectedVersion == null || _expectedVersion.equals(browserDefinition.getVersion())) {
                        if (_expectedPlatform == null || _expectedPlatform.equals(browserDefinition.getPlatform())) {
                            if (_expectedPlatformVersion == null || _expectedPlatformVersion.equals(browserDefinition.getPlatformVersion())) {
                                if (_expectedIsCrawler == null || _expectedIsCrawler == browserDefinition.isCrawler()) {
                                    if (_expectedIsMobile == null || _expectedIsMobile == browserDefinition.isMobileDevice()) {
                                        if (_expectedIsTablet == null || _expectedIsTablet == browserDefinition.isTablet()) {
                                            result = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return result;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE));
        }

        public BrowserDefinitionMatcher withBrowserName(String expectedBrowserName) {
            _expectedBrowserName = expectedBrowserName;
            return this;
        }

        public BrowserDefinitionMatcher withDeviceName(String expectedDeviceName) {
            _expectedDeviceName = expectedDeviceName;
            return this;
        }

        public BrowserDefinitionMatcher withVersion(String expectedVersion) {
            _expectedVersion = expectedVersion;
            return this;
        }

        public BrowserDefinitionMatcher withPlatform(String expectedPlatform) {
            _expectedPlatform = expectedPlatform;
            return this;
        }

        public BrowserDefinitionMatcher withPlatformVersion(String expectedPlatformVersion) {
            _expectedPlatformVersion = expectedPlatformVersion;
            return this;
        }

        public BrowserDefinitionMatcher whichIsACrawler() {
            _expectedIsCrawler = true;
            return this;
        }

        public BrowserDefinitionMatcher whichIsNotACrawler() {
            _expectedIsCrawler = false;
            return this;
        }

        public BrowserDefinitionMatcher whichIsAMobileDevice() {
            _expectedIsMobile = true;
            return this;
        }

        public BrowserDefinitionMatcher whichIsNotAMobileDevice() {
            _expectedIsMobile = false;
            return this;
        }

        public BrowserDefinitionMatcher whichIsATablet() {
            _expectedIsTablet = true;
            return this;
        }

        public BrowserDefinitionMatcher whichIsNotATablet() {
            _expectedIsTablet = false;
            return this;
        }
    }

}
