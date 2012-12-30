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

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.echocat.jomon.net.http.browsers.BrowserDefinitionParser.Row;
import org.echocat.jomon.runtime.iterators.CloseableIterator;
import org.echocat.jomon.runtime.iterators.ConvertingIterator;
import org.echocat.jomon.runtime.util.Entry;
import org.echocat.jomon.runtime.util.Entry.Impl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.echocat.jomon.net.http.HttpUtils.makeGetRequestAndReturnBodyReaderFor;

/**
 * <p> This will return a {@link Rows PlainBrowserDefinitions} which implements {@link CloseableIterator} to iterate over all
 * {@link Row PlainBrowserDefinition} entries.</p>
 *
 * <p>If the {@link #iterator()} method is called it tries to load the latest browser definitions from the url given by
 * {@link #BrowserDefinitionParser(URL)}.</p>
 */
class BrowserDefinitionParser implements Iterable<Row> {

    static final URL DEFAULT_BROWSER_DEFINITION_FILE = BrowserDefinitionFactory.class.getResource("defaultBrowserDefinitions.csv");
    static final String BROWSER_DEFINITION_DATE_PATTERN = "EEE, d MMM yyyy hh:mm:ss Z";

    class Rows implements CloseableIterator<Row> {

        private final CSVReader _csvReader;
        private final long _version;
        private final Date _date;
        private final Map<String, Integer> _columnNamesToIndex;

        private Boolean _hasNext;
        private String[] _next;

        private Rows(@Nonnull CSVReader csvReader) throws Exception {
            _csvReader = csvReader;
            sanityCheckOfHeaderLines(csvReader);
            _version =  Long.valueOf(_next[0]);
            _date =  new SimpleDateFormat(BROWSER_DEFINITION_DATE_PATTERN, Locale.US).parse(_next[1]);
            _columnNamesToIndex = parseColumnNames(csvReader);
            sanityCheckOfDefaultBrowserDefinition();
        }

        @Override
        public boolean hasNext() {
            if (_hasNext == null) {
                try {
                    _next = _csvReader.readNext();
                    _hasNext = _next != null;
                } catch (IOException e) {
                    throw new RuntimeException("Could not read next line from " + _csvReader + ".", e);
                }
                if (_hasNext && _next.length != _columnNamesToIndex.size()) {
                    throw new IllegalArgumentException(_csvReader + " contains an invalid format.");
                }
            }
            return _hasNext;
        }

        @Override
        public Row next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            final Row plainBrowserDefinition = new Row(asList(_next), _columnNamesToIndex);
            _hasNext = null;
            _next = null;
            return plainBrowserDefinition;
        }

        long getVersion() { return _version; }
        Date getDate() { return _date; }
        Set<String> getColumnNames() { return _columnNamesToIndex.keySet(); }
        @Override public void close() { closeQuietly(_csvReader); }
        @Override public void remove() { throw new UnsupportedOperationException(); }

        @Nonnull
        private Map<String, Integer> parseColumnNames(@Nonnull CSVReader csvReader) throws IOException {
            final Map<String, Integer> columnNamesToIndex = new HashMap<>();
            final String[] columns = csvReader.readNext();
            for (int index = 0; index < columns.length; index++) {
                columnNamesToIndex.put(columns[index], index);
            }
            return unmodifiableMap(columnNamesToIndex);
        }

        private void sanityCheckOfHeaderLines(@Nonnull CSVReader csvReader) throws IOException {
            final String[] columns = csvReader.readNext();
            if (columns.length != 2 || !EXPECTED_FIRST_COLUMNS_CONTENT.equals(columns[0]) || !EXPECTED_FIRST_COLUMNS_CONTENT.equals(columns[1])) {
                throw new IllegalArgumentException(csvReader + " contains an invalid format.");
            }
            final String[] secondColumns = csvReader.readNext();
            if (secondColumns.length != 2) {
                throw new IllegalArgumentException(csvReader + " contains an invalid format.");
            }
            _next = secondColumns;
        }

        private void sanityCheckOfDefaultBrowserDefinition() {
            if (!hasNext()) {
                throw new IllegalArgumentException(_csvReader + " contains an invalid format.");
            }
            final Row next = next();
            if (!DEFAULT_PROPERTIES_CONTENT.equals(next.getValue("Parent")) || !DEFAULT_PROPERTIES_CONTENT.equals(next.getValue("Browser")) || !(DEFAULT_PROPERTIES_CONTENT ).equals(next.getValue("PropertyName"))) {
                throw new IllegalArgumentException(_csvReader + " contains an invalid format.");
            }
        }

        @Override public String toString() { return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE); }
    }

    class Row implements Iterable<Entry<String, String>> {
        private final List<String> _row;
        private final Map<String, Integer> _columnNamesToIndex;

        private Row(@Nonnull List<String> row, @Nonnull Map<String, Integer> columnNamesToIndex) {
            _row = row;
            _columnNamesToIndex = columnNamesToIndex;
        }

        @Override
        public Iterator<Entry<String, String>> iterator() {
            return new ConvertingIterator<Map.Entry<String, Integer>, Entry<String, String>>(_columnNamesToIndex.entrySet().iterator()) { @Override protected Entry<String, String> convert(Map.Entry<String, Integer> input) {
                final String value = _row.get(input.getValue());
                return new Impl<>(input.getKey(), value);
            }};
        }

        @Nullable
        String getValue(@Nonnull String key) {
            final Integer index = _columnNamesToIndex.get(key);
            return index != null ? _row.get(index) : null;
        }

        @Override public String toString() { return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE); }
    }

    private static final Logger LOG = LoggerFactory.getLogger(BrowserDefinitionParser.class);
    private static final String EXPECTED_FIRST_COLUMNS_CONTENT = "GJK_Browscap_Version";
    private static final String DEFAULT_PROPERTIES_CONTENT = "DefaultProperties";

    private final URL _browserDefinitionFile;

    BrowserDefinitionParser(@Nonnull URL browserDefinitionFile) {
        _browserDefinitionFile = browserDefinitionFile;
    }

    @Override
    public Rows iterator() {
        Rows result;
        try {
            result = getFromUrl(_browserDefinitionFile);
        } catch (Exception e) {
            LOG.warn("Could not open the browserDefinition file '" + _browserDefinitionFile + "' which was bufferred from original remote location. Fallback to default version.", e);
            try {
                result = getFromUrl(DEFAULT_BROWSER_DEFINITION_FILE);
            } catch (Exception secondException) {
                throw new RuntimeException("Could not load default browserDefinition file from: " + DEFAULT_BROWSER_DEFINITION_FILE, secondException);
            }
        }
        return result;
    }

    @Nonnull
    Rows getFromUrl(@Nonnull URL browserDefinitionFile) throws Exception {
        final Rows result;
        boolean success = false;
        final Reader reader;
        if ("file".equals(browserDefinitionFile.getProtocol())) {
            final File file = new File(browserDefinitionFile.toURI());
            reader = new FileReader(file);
        } else if ("jar".equals(browserDefinitionFile.getProtocol())) {
            reader = new InputStreamReader(browserDefinitionFile.openStream());
        } else {
            reader = makeGetRequestAndReturnBodyReaderFor(browserDefinitionFile);
        }
        try {
            result = get(reader);
            success = true;
        } finally {
            if (!success) {
                closeQuietly(reader);
            }
        }
        return result;
    }

    @Nonnull
    private Rows get(@Nonnull Reader reader) throws Exception {
        boolean success = false;
        final CSVReader cr = new CSVReader(reader, ',', '"');
        try {
            final Rows result = new Rows(cr);
            success = true;
            return result;
        } finally {
            if (!success) {
                closeQuietly(cr);
            }
        }
    }
}
