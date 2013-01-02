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

package org.echocat.jomon.runtime.format;

import org.apache.commons.io.IOUtils;
import org.echocat.jomon.runtime.CollectionUtils;
import org.echocat.jomon.runtime.format.Target.Format;
import org.echocat.jomon.runtime.util.Hint;
import org.echocat.jomon.runtime.util.Hints;
import org.eclipse.mylyn.wikitext.confluence.core.ConfluenceLanguage;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguageConfiguration;
import org.eclipse.mylyn.wikitext.mediawiki.core.MediaWikiLanguage;
import org.eclipse.mylyn.wikitext.textile.core.TextileLanguage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.lang3.StringEscapeUtils.ESCAPE_HTML4;
import static org.echocat.jomon.runtime.format.Source.Format.html;
import static org.echocat.jomon.runtime.format.Source.Format.plain;
import static org.echocat.jomon.runtime.util.Hint.Impl.hint;
import static org.echocat.jomon.runtime.util.Hints.hints;
import static org.echocat.jomon.runtime.util.Hints.nonNullHints;

@SuppressWarnings("ConstantNamingConvention")
public class MylynWikitextFormatter extends FormatterSupport {

    public static final Hint<MarkupLanguageConfiguration> markupLanguageConfiguration = hint(MarkupLanguageConfiguration.class, "markupLanguageConfiguration");
    public static final Hint<Boolean> formatOutput = hint(Boolean.class, "formatOutput", false);

    private static final Map<Source.Format, Class<? extends MarkupLanguage>> SOURCE_FORMAT_TO_LANGUAGE_TYPE = unmodifiableMap(CollectionUtils.<Source.Format, Class<? extends MarkupLanguage>>asMap(
        Source.Format.confluence, ConfluenceLanguage.class,
        Source.Format.mediaWiki, MediaWikiLanguage.class,
        Source.Format.textile, TextileLanguage.class
    ));

    @Override
    public void format(@Nonnull Source source, @Nonnull Target target, @Nullable Hints hints) throws IllegalArgumentException, IOException {
        final Hints targetHints = nonNullHints(hints);
        final Source.Format sourceFormat = source.getFormat();
        if (sourceFormat.equals(plain) || sourceFormat.equals(html)) {
            formatWithoutInterpretation(source, target);
        } else {
            formatWithInterpretation(source, target, targetHints);
        }
    }

    protected void formatWithoutInterpretation(@Nonnull Source source, @Nonnull Target target) throws IOException {
        final Source.Format sourceFormat = source.getFormat();
        final Target.Format targetFormat = target.getFormat();
        if (sourceFormat.equals(plain) && targetFormat.equals(Format.html)) {
            final String input = IOUtils.toString(source.getReader());
            ESCAPE_HTML4.translate(input, target.getWriter());
        } else if (sourceFormat.equals(html) && targetFormat.equals(Format.plain)) {
            htmlToPlainText(source, target);
        } else {
            copy(source, target);
        }
    }

    protected void formatWithInterpretation(@Nonnull Source source, @Nonnull Target target, @Nonnull Hints hints) throws IOException {
        final MarkupParser markupParser = new MarkupParser();
        markupParser.setMarkupLanguage(createLanguageFor(source, hints));
        final boolean pipeThroughHtmlToPlainText = target.getFormat().equals(Format.plain);
        final Writer writer = pipeThroughHtmlToPlainText ?  new StringWriter() : target.getWriter();
        markupParser.setBuilder(createDocumentBuilder(target, writer, hints));
        markupParser.parse(source.getReader(), false);
        if (pipeThroughHtmlToPlainText) {
            FormatUtils.htmlToPlainText(new StringReader(writer.toString()), target.getWriter());
        }
    }

    @SuppressWarnings("UnusedParameters")
    @Nonnull
    protected MarkupLanguage createLanguageFor(@Nonnull Source source, @Nonnull Hints hints) {
        return createLanguageFor(source.getFormat());
    }

    @Nonnull
    protected MarkupLanguage createLanguageFor(@Nonnull Source.Format format) {
        final Class<? extends MarkupLanguage> type = SOURCE_FORMAT_TO_LANGUAGE_TYPE.get(format);
        if (type == null) {
            throw new IllegalArgumentException("Could not handle source format " + format + ".");
        }
        final MarkupLanguage markupLanguage;
        try {
            markupLanguage = type.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Could not create a new instance of " + type.getName() + " to handle source of format " + format + ".", e);
        }
        markupLanguage.configure(hints().get(markupLanguageConfiguration, new MarkupLanguageConfiguration()));
        return markupLanguage;
    }

    @Nonnull
    protected DocumentBuilder createDocumentBuilder(@Nonnull Target target, @Nonnull Writer writer, @Nonnull Hints hints) throws IOException {
        final Target.Format format = target.getFormat();
        final DocumentBuilder result;
        if (format.equals(Format.html) || format.equals(Format.plain)) {
            result = new HtmlDocumentBuilder(writer, hints.get(formatOutput));
        } else {
            throw new IllegalArgumentException("Could not handle target format " + format + ".");
        }
        return result;
    }

    protected void htmlToPlainText(@Nonnull Source from, @Nonnull Target to) throws IOException {
        FormatUtils.htmlToPlainText(from.getReader(), to.getWriter());
    }

    protected void copy(@Nonnull Source from, @Nonnull Target to) throws IOException {
        IOUtils.copy(from.getReader(), to.getWriter());
    }


}
