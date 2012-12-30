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

package org.echocat.jomon.runtime.util;

public class GlobPattern {

    /**
     * Pattern for matching 0-n.
     */
    public static final char GLOB_MULTIPLE = '*';
    /**
     * Pattern for matching 1.
     */
    public static final char GLOB_SINGLE = '?';
    /**
     * Type of subpattern.
     */
    public static final char TEXT = 't';

    private char _type;
    private String _text;
    private char[] _chars;
    private int _textLength;
    /**
     * The absolut position of the next text element.
     */
    private GlobPattern _nextTextElement;

    public GlobPattern(char type) {
        _type = type;
    }

    public GlobPattern(char type, String text) {
        this(type);
        setText (text);
    }

    public String getText() {
        return _text;
    }

    public void setText(String text) {
        _text = text;
        _chars = text.toCharArray();
        _textLength = text.length();
    }

    public char[] getChars() {
        return _chars;
    }

    public int getTextLength() {
        return _textLength;
    }

    public char getType() {
        return _type;
    }

    public void setType(char type) {
        _type = type;
    }

    public GlobPattern getNextTextElement() {
        return _nextTextElement;
    }

    public void setNextTextElement(GlobPattern nextTextElement) {
        _nextTextElement = nextTextElement;
    }

    @Override
    public String toString() {

        String s = "[" + _type;
        if (_type == TEXT) {
            s += "-" + _text;
        }
        s += "]";
        return s;
    }

}
