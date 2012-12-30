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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.echocat.jomon.net.http.browsers.BrowserDefinitionParser.Row;
import org.echocat.jomon.net.http.browsers.BrowserDefinitionParser.Rows;
import org.echocat.jomon.runtime.CollectionUtils;
import org.echocat.jomon.testing.environments.LogEnvironment;
import org.echocat.jomon.testing.io.DummyUrlFactory;
import org.hamcrest.Description;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.echocat.jomon.net.http.browsers.BrowserDefinitionParser.BROWSER_DEFINITION_DATE_PATTERN;
import static org.echocat.jomon.testing.Assert.assertThat;

public class BrowserDefinitionParserUnitTest {

    private static final String TEST_BROWSER_DEFINITION_DATE = "Wed, 21 Jun 2011 23:26:51 -0000";

    @Rule
    public final LogEnvironment _logEnvironment = new LogEnvironment();

    @Test
    public void testFallbackToClassPathVersionIfFileFactoryDoReturnIllegalFile() throws Exception {
        try (final Rows definitions = createDefinitionsFactoryWith("inWrongFormat").iterator()) {
            assertThat(definitions, isDefinitionsFromClassPath());
        }
    }

    @Test
    public void testToLoadFromRemoteFile() throws Exception {
        try (final Rows definitions = createDefinitionsFactoryWith(BrowserDefinitionParser.class.getResource("testBrowserDefinitions.csv")).iterator()) {
            assertThat(definitions, isDefinitions().withVersion(666).withDate(TEST_BROWSER_DEFINITION_DATE)
                .withRowThatContains(
                    "Parent", "Ask",
                    "PropertyName", "Ask",
                    "Browser", "Ask"
                ).withRowThatContains(
                    "Parent", "MSN",
                    "PropertyName", "MSN",
                    "Browser", "MSN"
                ).withRowThatContains(
                    "Parent", "IE 9.0",
                    "PropertyName", "Mozilla/5.0 (compatible; MSIE 9.0; *Windows NT 6.1; WOW64; Trident/5.0*)*",
                    "Browser", ""
                ).withRowThatContains(
                    "Parent", "*",
                    "PropertyName", "*",
                    "Browser", "Default Browser"
                ));
        }
    }

    private BrowserDefinitionParser createDefinitionsFactoryWith(URL browserDefinitionsFile) throws Exception {
        return new BrowserDefinitionParser(browserDefinitionsFile);
    }

    private BrowserDefinitionParser createDefinitionsFactoryWith(String browserDefinitionsFileContent) throws Exception {
        final URL url = DummyUrlFactory.get(browserDefinitionsFileContent, "UTF-8");
        return createDefinitionsFactoryWith(url);
    }

    private BrowserDefinitionsParserMatcher isDefinitions() {
        return new BrowserDefinitionsParserMatcher();
    }

    private BrowserDefinitionsParserMatcher isDefinitionsFromClassPath() throws Exception {
        return isDefinitions().withVersion(0);
    }

    private static class BrowserDefinitionsParserMatcher extends TypeSafeMatcher<Rows> {

        private long _expectedVersion;
        private Date _expectedDate;
        private List<Map<String, String>> _rowsWithValues;

        @Override
        public boolean matchesSafely(Rows definitions) {
            boolean result = false;
            if (definitions != null) {
                if (definitions.getVersion() == _expectedVersion) {
                    if (_expectedDate == null || definitions.getDate().equals(_expectedDate)) {
                        result = true;
                        if (_rowsWithValues != null) {
                            final Iterator<Map<String, String>> expectedRows = _rowsWithValues.iterator();
                            while (result && definitions.hasNext() && expectedRows.hasNext()) {
                                final Row definition = definitions.next();
                                final Iterator<Map.Entry<String, String>> expectedRowEntries = expectedRows.next().entrySet().iterator();
                                while (result && expectedRowEntries.hasNext()) {
                                    final Entry<String, String> expectedEntry = expectedRowEntries.next();
                                    final String key = expectedEntry.getKey();
                                    final String value = definition.getValue(key);
                                    result = expectedEntry.getValue().equals(value);
                                }
                            }
                            if (result) {
                                result = definitions.hasNext() == expectedRows.hasNext();
                            }
                        }
                    }
                }
            }
            return result;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE));
        }

        public BrowserDefinitionsParserMatcher withVersion(long expectedVersion) {
            _expectedVersion = expectedVersion;
            return this;
        }

        public BrowserDefinitionsParserMatcher withDate(String expectedDateString) throws ParseException {
            return withDate(new SimpleDateFormat(BROWSER_DEFINITION_DATE_PATTERN, Locale.US).parse(expectedDateString));
        }

        public BrowserDefinitionsParserMatcher withDate(Date expectedDate) {
            _expectedDate = expectedDate;
            return this;
        }

        public BrowserDefinitionsParserMatcher withRowThatContains(String... values) {
            withRowThatContains(CollectionUtils.<String, String>asMap(values));
            return this;
        }

        public BrowserDefinitionsParserMatcher withRowThatContains(Map<String, String> values) {
            if (_rowsWithValues == null) {
                _rowsWithValues = new ArrayList<>();
            }
            _rowsWithValues.add(values);
            return this;
        }
    }
}
