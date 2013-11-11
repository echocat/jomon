package org.echocat.jomon.runtime.numbers;

import org.echocat.jomon.runtime.annotations.Excluding;
import org.echocat.jomon.runtime.annotations.Including;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import static javax.xml.bind.JAXBContext.newInstance;
import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import static org.echocat.jomon.runtime.numbers.LongRangeUnitTest.XmlTest.testElement;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LongRangeUnitTest {

    public static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
        "<xmlTest>\n" +
        "    <longRange from=\"-666\" to=\"666\"/>\n" +
        "</xmlTest>\n";

    @Test
    public void testMarshall() throws Exception {
        try (final Writer writer = new StringWriter()) {
            marshaller().marshal(testElement(), writer);
            assertThat(writer.toString(), is(XML));
        }
    }

    @Test
    public void testUnmarshall() throws Exception {
        try (final Reader reader = new StringReader(XML)) {
            final Object element = unmarshaller().unmarshal(reader);
            assertThat(element, CoreMatchers.<Object>is(testElement()));
        }
    }

    @Nonnull
    protected static Marshaller marshaller() throws JAXBException {
        final JAXBContext context = context();
        final Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);
        return marshaller;
    }

    @Nonnull
    protected static Unmarshaller unmarshaller() throws JAXBException {
        final JAXBContext context = context();
        return context.createUnmarshaller();
    }

    @Nonnull
    protected static JAXBContext context() throws JAXBException {
        return newInstance(XmlTest.class);
    }

    @XmlRootElement(name = "xmlTest")
    public static class XmlTest {

        @Nonnull
        public static XmlTest testElement() {
            return testElement(-666L, 666L);
        }

        @Nonnull
        public static XmlTest testElement(@Nullable @Including Long from, @Nullable @Excluding Long to) {
            final LongRange range = new LongRange(from, to);
            final XmlTest element = new XmlTest();
            element.setRange(range);
            return element;
        }

        private LongRange _range;

        @XmlElement(name = "longRange")
        public LongRange getRange() {
            return _range;
        }

        public void setRange(LongRange range) {
            _range = range;
        }


        @Override
        public boolean equals(Object o) {
            final boolean result;
            if (this == o) {
                result = true;
            } else if (o == null || getClass() != o.getClass()) {
                result = false;
            } else {
                final XmlTest that = (XmlTest) o;
                result = _range != null ? _range.equals(that._range) : that._range == null;
            }
            return result;
        }

        @Override
        public int hashCode() {
            return _range != null ? _range.hashCode() : 0;
        }

    }

}
