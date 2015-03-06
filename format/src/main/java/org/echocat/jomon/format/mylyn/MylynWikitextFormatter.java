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

package org.echocat.jomon.format.mylyn;

import org.echocat.jomon.format.FormatUtils;
import org.echocat.jomon.format.FormatterSupport;
import org.echocat.jomon.format.Source;
import org.echocat.jomon.format.Target;
import org.echocat.jomon.format.Target.Format;
import org.echocat.jomon.runtime.util.Hints;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.lang.Thread.currentThread;
import static org.apache.commons.lang3.StringUtils.join;
import static org.echocat.jomon.format.mylyn.MylynAssignmentsSupport.findType;
import static org.echocat.jomon.format.mylyn.MylynDocumentBuilders.findDocumentBuilderType;
import static org.echocat.jomon.format.mylyn.MylynMarkupLanguages.findMarkupLanguageType;

public class MylynWikitextFormatter extends FormatterSupport {

    public static final String MARKUP_PARSER_TYPE_NAME = "org.eclipse.mylyn.wikitext.core.parser.MarkupParser";

    private final MylynMarkupLanguages _markupLanguages;
    private final MylynDocumentBuilders _documentBuilders;

    private final Class<?> _markupParserType;
    private final Class<?> _documentBuilderType;
    private final Class<?> _markupLanguageType;

    private final Method _setMarkupLanguage;
    private final Method _setBuilder;
    private final Method _parse;

    private final boolean _validLoaded;

    public MylynWikitextFormatter(@Nullable ClassLoader classLoader, @Nullable MylynMarkupLanguages markupLanguages, @Nullable MylynDocumentBuilders documentBuilders) {
        final ClassLoader targetClassLoader = classLoader != null ? classLoader : currentThread().getContextClassLoader();
        _markupLanguages = markupLanguages != null ? markupLanguages : new MylynMarkupLanguages(targetClassLoader);
        _documentBuilders = documentBuilders != null ? documentBuilders : new MylynDocumentBuilders(targetClassLoader);

        _markupParserType = findType(targetClassLoader, MARKUP_PARSER_TYPE_NAME);
        _documentBuilderType = findDocumentBuilderType(targetClassLoader);
        _markupLanguageType = findMarkupLanguageType(targetClassLoader);

        _setMarkupLanguage = _markupParserType != null && _markupLanguageType != null ? loadMethod(_markupParserType, "setMarkupLanguage", _markupLanguageType) : null;
        _setBuilder = _markupParserType != null && _documentBuilderType != null ? loadMethod(_markupParserType, "setBuilder", _documentBuilderType) : null;
        _parse = _markupParserType != null ? loadMethod(_markupParserType, "parse", Reader.class, boolean.class) : null;

        _validLoaded = _documentBuilderType != null && _markupLanguageType != null && _setMarkupLanguage != null && _setBuilder != null && _parse != null;
    }

    public MylynWikitextFormatter() {
        this(null, null, null);
    }

    @Override
    public void format(@Nonnull Source source, @Nonnull Target target, @Nullable Hints hints) throws IllegalArgumentException, IOException {
        if (!canHandle(source, target, hints)) {
            throw new IllegalArgumentException("Could not handle the combination of " + source + " and " + target + ".");
        }
        final Format requestedTargetFormat = target.getFormat();
        final Format targetFormat = Target.Format.textPlain.equals(requestedTargetFormat) ? Target.Format.html : requestedTargetFormat;
        final Writer writer = targetFormat.equals(requestedTargetFormat) ? target.getWriter() : new StringWriter();
        final Object markupLanguage = createMarkupLanguageFor(source);
        final Object documentBuilder = createDocumentBuilderFor(targetFormat, writer);
        final Object markupParser = createMarkupParser(markupLanguage, documentBuilder);
        formatWith(source.getReader(), markupParser);
        if (!targetFormat.equals(requestedTargetFormat)) {
            FormatUtils.htmlToPlainText(new StringReader(writer.toString()), target.getWriter());
        }
    }

    protected void formatWith(@Nonnull Reader reader, @Nonnull Object markupParser) throws IOException {
        try {
            _parse.invoke(markupParser, reader, false);
        } catch (final InvocationTargetException e) {
            final Throwable target = e.getTargetException();
            if (target instanceof IOException) {
                //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw (IOException) target;
            } else if (target instanceof RuntimeException) {
                //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw (RuntimeException) target;
            } else if (target instanceof Error) {
                //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw (Error) target;
            } else {
                throw new RuntimeException("Could not parse.", target != null ? target : e);
            }
        } catch (final Exception e) {
            throw new RuntimeException("Could not parse.", e);
        }
    }

    @Nonnull
    protected Object createMarkupParser(@Nonnull Object markupLanguage, @Nonnull Object documentBuilder) {
        try {
            final Object markupParser = _markupParserType.newInstance();
            _setMarkupLanguage.invoke(markupParser, markupLanguage);
            _setBuilder.invoke(markupParser, documentBuilder);
            return markupParser;
        } catch (final Exception e) {
            throw new IllegalStateException("Could not create parser.", e);
        }
    }

    @Nonnull
    protected Object createMarkupLanguageFor(@Nonnull Source source) throws IllegalArgumentException {
        return createLanguageFor(source.getFormat());
    }

    @Nonnull
    protected Object createLanguageFor(@Nonnull Source.Format format) throws IllegalArgumentException {
        final Class<?> type = _markupLanguages.findTypeFor(format);
        if (type == null) {
            throw new IllegalArgumentException("Could not handle source format " + format + ".");
        }
        try {
            return type.newInstance();
        } catch (final Exception e) {
            throw new RuntimeException("Could not create a new instance of " + type.getName() + " to handle source of format " + format + ".", e);
        }
    }

    @Nonnull
    protected Object createDocumentBuilderFor(@Nonnull Target.Format format, @Nonnull Writer writer) throws IOException {
        final Class<?> type = _documentBuilders.findTypeFor(format);
        if (type == null) {
            throw new IllegalArgumentException("Could not handle target format " + format + ".");
        }
        try {
            final Constructor<?> constructor = type.getConstructor(Writer.class);
            return constructor.newInstance(writer);
        } catch (final Exception e) {
            throw new RuntimeException("Could not create a new instance of " + type.getName() + " to handle target of format " + format + ".", e);
        }
    }

    @Override
    public boolean canHandle(@Nonnull Source source, @Nonnull Target target, @Nullable Hints hints) {
        return _validLoaded && canHandle(source) && canHandle(target);
    }

    protected boolean canHandle(@Nonnull Source source) {
        return _markupLanguages.findTypeFor(source.getFormat()) != null;
    }

    protected boolean canHandle(@Nonnull Target target) {
        final Format requestedTargetFormat = target.getFormat();
        final Format targetFormat = Target.Format.textPlain.equals(requestedTargetFormat) ? Target.Format.html : requestedTargetFormat;
        return _documentBuilders.findTypeFor(targetFormat) != null;
    }

    @Nonnull
    protected static Method loadMethod(@Nonnull Class<?> from, @Nonnull String name, @Nullable Class<?>... parameterTypes) {
        try {
            return from.getMethod(name, parameterTypes);
        } catch (final NoSuchMethodException ignored) {
            throw new IllegalStateException("Could not find the method " + name + "(" + join(parameterTypes)  + ") at " + from.getName() + ".");
        }
    }
}
