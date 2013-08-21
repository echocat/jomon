package org.echocat.jomon.process.daemon.listeners.support;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.unmodifiableMap;
import static java.util.Locale.US;
import static java.util.regex.Pattern.compile;

public class DefaultMessageFormatter implements MessageFormatter {

    private static final Pattern PARAMETER_REPLACE_PATTERN = compile("\\{([a-zA-Z0-9]+)([,}])");

    @Nonnull
    public static DefaultMessageFormatter messageFormatterFor(@Nonnull String pattern, @Nonnull Locale locale, @Nullable String... keys) {
        return new DefaultMessageFormatter(pattern, locale, keys);
    }

    @Nonnull
    public static DefaultMessageFormatter messageFormatterFor(@Nonnull String pattern, @Nullable String... keys) {
        return messageFormatterFor(pattern, US, keys);
    }

    private final Map<String, Integer> _parameterKeyToIndex;
    private final MessageFormat _messageFormat;

    public DefaultMessageFormatter(@Nonnull String pattern, @Nullable String... keys) {
        this(pattern, US, keys);
    }

    public DefaultMessageFormatter(@Nonnull String pattern, @Nonnull Locale locale, @Nullable String... keys) {
        _parameterKeyToIndex = toParameterKeyToIndex(keys);
        _messageFormat = toMessageFormat(pattern, locale, _parameterKeyToIndex);
    }

    @Override
    @Nonnull
    public String format(@Nonnull Map<String, Object> parameters) {
        final Object[] values = toParameterValues(parameters, _parameterKeyToIndex);
        return _messageFormat.format(values);
    }

    @Nonnull
    protected MessageFormat toMessageFormat(@Nonnull String pattern, @Nonnull Locale locale, @Nonnull Map<String, Integer> parameterKeyToIndex) {
        final StringBuffer sb = new StringBuffer();
        final Matcher matcher = PARAMETER_REPLACE_PATTERN.matcher(pattern);
        while (matcher.find()) {
            final String key = matcher.group(1);
            final Integer index = parameterKeyToIndex.get(key);
            if (index != null) {
                matcher.appendReplacement(sb, "{" + index + "$2");
            }
        }
        matcher.appendTail(sb);
        return new MessageFormat(sb.toString(), locale);
    }

    @Nonnull
    protected Object[] toParameterValues(@Nonnull Map<String, Object> parameters, @Nonnull Map<String, Integer> parameterKeyToIndex) {
        final Object[] result = new Object[parameterKeyToIndex.size() + 1];
        for (Entry<String, Integer> parameterKeyAndIndex : parameterKeyToIndex.entrySet()) {
            result[parameterKeyAndIndex.getValue()] = parameters.get(parameterKeyAndIndex.getKey());
        }
        result[result.length - 1] = null;
        return result;
    }

    @Nonnull
    protected Map<String, Integer> toParameterKeyToIndex(@Nullable String... keys) {
        final Map<String, Integer> result = new HashMap<>();
        if (keys != null) {
            int i = 0;
            for (String key : keys) {
                result.put(key, i++);
            }
        }
        return unmodifiableMap(result);
    }


}
