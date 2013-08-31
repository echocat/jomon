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

package org.echocat.jomon.runtime.util;

import javax.annotation.Nonnull;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.*;
import static org.echocat.jomon.runtime.StringUtils.*;
import static org.echocat.jomon.runtime.util.GlobPattern.*;

/**
 * A simple (but fast ;) pattern-matcher. Matches strings against a pattern
 * that may contain the wildcards '*' and '?'.
 *
 * Occurences of the wildcards can be escaped using '\'. The '\' has to be
 * escaped by '\\'.
 * Matching of '*' is non-greedy. '*' matches 0 or more, '?' matches exact one.
 */
public class Glob {

    private String _patternSource;
    /**
     * A List of GlobPattern objects. 
     */
    private GlobPattern[] _compiledPattern;
    /**
     * If true String matching will be done case insensitivly.
     */
    private final boolean _caseInSensitive;

    /**
     * @return true if the given pattern contains globbing AND is valid.
     */
    public static boolean containsGlob(String pattern) {
        boolean result = false;
        try {
            final Glob glob = new Glob(pattern);
            result = glob.containsGlob();
        } catch (ParseException e) {
            // Ignored
        }
        return result;
    }

    public Glob(String pattern) throws ParseException {
        this(pattern, false);
    }

    public Glob(String pattern, boolean caseInSensitive) throws ParseException {
        _patternSource = pattern;
        _caseInSensitive = caseInSensitive;
        compilePattern();
    }

    /**
     * Match the given string.
     */
    public boolean matches(String string) {
        String src = string;
        final boolean result;
        if (_compiledPattern.length == 1) {

            // Shortcut for pattern '*', '?' and patterns not containing any wildcard
            final GlobPattern pattern = _compiledPattern[0];
            if (pattern.getType() == GLOB_MULTIPLE) {
                result = true;
            } else if (pattern.getType() == GLOB_SINGLE) {
                result = src.length() == 1;
            } else {
                if (_caseInSensitive) {
                    result = src.equalsIgnoreCase(pattern.getText());
                } else {
                    result = src.equals(pattern.getText());
                }
            }
            
        } else if (_compiledPattern.length == 2) {

            // Shortcuts for common patterns '*something' and 'something*'
            final GlobPattern pattern1 = _compiledPattern[0];
            final GlobPattern pattern2 = _compiledPattern[1];
            if (_caseInSensitive) {
                src = src.toLowerCase();
            }
            if (pattern1.getType() == TEXT) {
                 result = src.startsWith(pattern1.getText()) && (pattern2.getType() == GLOB_MULTIPLE || src.length() == pattern1.getTextLength()+1);
            } else {
                result = src.endsWith(pattern2.getText()) && (pattern1.getType() == GLOB_MULTIPLE || src.length() == pattern2.getTextLength()+1);
            }

        } else {
            result = matches(src.toCharArray());
        }
        return result;
    }

    /**
     * Match the given char-Array.
     */
    public boolean matches(char[] src) {

        if (_caseInSensitive) {
            toLowerCase(src);
        }

        return matchIntern(src, 0, 0);
    }

    @SuppressWarnings({"MethodWithMultipleReturnPoints", "ValueOfIncrementOrDecrementUsed"})
    private boolean matchIntern(char[] csrc, int pos, int currentSpos) {
        GlobPattern pattern;
        int spos = currentSpos;
        for (int p = pos; p < _compiledPattern.length; p++) {
            pattern = _compiledPattern[p];
            if (pattern.getType() == TEXT) {

                // Match the text
                if (!startsWith(csrc, pattern.getChars(), spos)) {
                    return false;
                }
            
                // Text match ok.
                spos += pattern.getTextLength();
            } else if (pattern.getType() == GLOB_SINGLE) {

                // Single globbing
                if (spos++ == csrc.length) {
                    return false;
                }
            } else {

                // Shortcut: No more text elements -> end it.
                if (pattern.getNextTextElement() == null) {
                    return true;
                }
                
                // Another shortcut: Is the next textnode
                final char[] chars = pattern.getNextTextElement().getChars();
                int substrPos = indexOf(csrc, chars, spos);
                
                // Go through all possible ways
                while (substrPos != -1) {

                    // Optimization: Is the next element after the textelement
                    // a multiglob? Or is it a singleglob and the length matches? 
                    // Then we are through
                    if (p == _compiledPattern.length - 3) {
                        final GlobPattern p2 = _compiledPattern[_compiledPattern.length - 1];
                        if (p2.getType() == GLOB_MULTIPLE) {
                            return true;
                        }
                        if (p2.getType() == GLOB_SINGLE && substrPos + chars.length == csrc.length - 1) {
                            return true;
                        }
                    }

                    // Optimization: Since we know that the next part is a textelement
                    // and we know that it fits -> we can skip that element
                    if (p < _compiledPattern.length - 2) {
                        substrPos += chars.length;
                    } else {

                        // Now we know that we are though! The next one is a textelement
                        // and it matches
                        return true;
                    }
                    if (matchIntern(csrc, p + 2, substrPos)) {
                        return true;
                    }
                    substrPos = indexOf(csrc, chars, substrPos + 1);
                }
                
                // Oh - it didn't work out.
                return false;
            }
        }
        return csrc.length == spos;
    }

    public boolean containsGlob() {
        return ! (getCompiledPattern().length == 1 && getCompiledPattern()[0].getType() == TEXT);
    }

    public boolean isCaseInSensitive() {
        return _caseInSensitive;
    }

    /**
     * Compile the pattern into the strings[] and wildcard[] arrays.
     */
    private void compilePattern() throws ParseException {
        StringBuilder sb = new StringBuilder();
        boolean escaped = false;
        final List<GlobPattern> patterns = new ArrayList<>();
        for (int a = 0; a < _patternSource.length(); a++) {
            final char c = _patternSource.charAt(a);
            
            // Test escape-char
            if (c == '\\') {
                if (escaped) {
                    sb.append('\\');
                    escaped = false;
                } else {
                    escaped = true;
                }
            } else {
                if (escaped) {
                    if (c != GLOB_SINGLE && c != GLOB_MULTIPLE) {
                        sb.append("\\");
                    } else {

                        // Globbing chars escaped
                        sb.append(c);
                        escaped = false;
                    }
                } else {

                    // No escape character, test whether it matches on of the globbing characters.
                    if (c == GLOB_MULTIPLE || c == GLOB_SINGLE) {
                        if (sb.length() > 0) {
                            patterns.add(new GlobPattern(TEXT, sb.toString()));
                            sb = new StringBuilder();
                        }
                        patterns.add(new GlobPattern(c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        if (sb.length() > 0) {
            patterns.add(new GlobPattern(TEXT, sb.toString()));
        }
        
        // Add meta information and correct the elements
        addMetaInformation(patterns);
        
        _compiledPattern = new GlobPattern[patterns.size()];
        patterns.toArray(_compiledPattern);
    }

    @SuppressWarnings({"ValueOfIncrementOrDecrementUsed", "ContinueStatement", "AssignmentToForLoopParameter", "BreakStatement"})
    private void addMetaInformation(List<GlobPattern> patterns) {
        for (int a = 0; a < patterns.size(); a++) {
            final GlobPattern p = patterns.get(a);
            if (_caseInSensitive && p.getType() == TEXT) {
                p.setText(p.getText().toLowerCase());
            }
            for (int b = a + 1; b < patterns.size(); b++) {
                final GlobPattern p2 = patterns.get(b);

                // Remove things like '**', '?*' or '*?'
                if (p.getType() == GLOB_MULTIPLE) {
                    if (p2.getType() == GLOB_MULTIPLE || p2.getType() == GLOB_SINGLE) {
                        patterns.remove(b--);
                        continue;
                    }
                }
                if (p.getType() == GLOB_SINGLE) {
                    if (p2.getType() == GLOB_MULTIPLE) {
                        patterns.remove(b--);
                        continue;
                    }
                }

                if (p2.getType() == TEXT) {
                    p.setNextTextElement(p2);
                    break;
                }
            }
        }
    }

    public GlobPattern[] getCompiledPattern() {
        return _compiledPattern;
    }

    @Nonnull
    public Pattern toPattern() {
        final StringBuilder sb = new StringBuilder();
        sb.append("^");
        for (GlobPattern pattern : _compiledPattern) {
            final char type = pattern.getType();
            if (type == GLOB_MULTIPLE) {
                sb.append(".*");
            } else if (type == GLOB_SINGLE) {
                sb.append(".");
            } else if (type == TEXT) {
                sb.append(quote(pattern.getText()));
            } else {
                throw new IllegalStateException("Don't know how to handle type " + type + ".");
            }
        }
        sb.append("$");
        return compile(sb.toString(), _caseInSensitive ? CASE_INSENSITIVE : 0);
    }

    public String getPatternSource() {
        return _patternSource;
    }

    public void setPatternSource(String patternSource) throws ParseException {
        _patternSource = patternSource;
        compilePattern();
    }

    @Override
    public String toString() {
        return _patternSource;
    }
}
