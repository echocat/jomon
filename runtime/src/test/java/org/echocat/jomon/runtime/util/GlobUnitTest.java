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

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class GlobUnitTest {
    @Test
    public void testCompileNormal() throws Exception {
        final Glob glob = new Glob("This*is*a?Test");
        assertThat(Arrays.toString(glob.getCompiledPattern()), is("[[t-This], [*], [t-is], [*], [t-a], [?], [t-Test]]"));
    }

    @Test
    public void testCompileEscaping() throws Exception {
        final Glob glob = new Glob("*my test\\\\ with \\* escaping\\??");
        assertThat(Arrays.toString(glob.getCompiledPattern()), is("[[*], [t-my test\\ with * escaping?], [?]]"));
    }

    @Test
    public void testCompileRemovalOfDoubleWildcards() throws Exception {
        // Test '**', '*?' and '?*'
        final Glob glob = new Glob("This ** should ?* be * removed *? x??y I * think?");
        final Glob simpleVersion = new Glob("This * should ? be * removed * x??y I * think?");
        assertThat(Arrays.toString(glob.getCompiledPattern()), is(Arrays.toString(simpleVersion.getCompiledPattern())));
    }

    @Test
    public void testSetPattern() throws Exception {
        final Glob glob = new Glob("foobar");
        glob.setPatternSource("This*is*a?Test");
        assertThat(Arrays.toString(glob.getCompiledPattern()), is("[[t-This], [*], [t-is], [*], [t-a], [?], [t-Test]]"));
    }

    @Test
    public void testNoWildcardMatching() throws Exception {
        final Glob glob = new Glob("Only a String.");
        assertThat(glob.matches("Only a String."), is(true));
        assertThat(glob.matches("Only a String. No not."), is(false));
        assertThat(glob.matches("only a String."), is(false));
    }

    @Test
    public void testEverythingMatching() throws Exception {
        final Glob glob = new Glob("*");
        assertThat(glob.matches("anything"), is(true));
        assertThat(glob.matches(""), is(true));
    }

    @Test
    public void testSingleCharMatching() throws Exception {
        final Glob glob = new Glob("?");
        assertThat(glob.matches("g"), is(true));
        assertThat(glob.matches(""), is(false));
        assertThat(glob.matches("12"), is(false));
    }

    @Test
    public void testStringWithQuestionMarkWildcard() throws Exception {
        final Glob glob = new Glob("Only?a?String.");
        assertThat(glob.matches("Only a String."), is(true));
        assertThat(glob.matches("Only-a-String."), is(true));
        assertThat(glob.matches("Onl a String."), is(false));
        assertThat(glob.matches("Only-a-String. No not."), is(false));
    }

    @Test
    public void testStringWithStarWildcard() throws Exception {
        final Glob glob = new Glob("Only*String.");
        assertThat(glob.matches("Only a String."), is(true));
        assertThat(glob.matches("Only whatsoever String."), is(true));
        assertThat(glob.matches("Only whatsoever String String."), is(true));
        assertThat(glob.matches("Only whatsoever string."), is(false));
    }

    @Test
    public void testMatchingOfComplexPatterns() throws Exception {
        final Glob glob = new Glob("This * is ? multi*test!");
        assertThat(glob.matches("This test is a multi-super-test!"), is(true));

        glob.setPatternSource("Ends with*");
        assertThat(glob.matches("Ends with something!"), is(true));
        assertThat(glob.matches("Ends with"), is(true));

        glob.setPatternSource("Ends with ?");
        assertThat(glob.matches("Ends with !"), is(true));
        assertThat(glob.matches("Ends with "), is(false));

        glob.setPatternSource("*Starts with");
        assertThat(glob.matches("Something Starts with"), is(true));
        assertThat(glob.matches("Starts with"), is(true));
        assertThat(glob.matches("starts with"), is(false));

        glob.setPatternSource("?Starts with");
        assertThat(glob.matches("!Starts with"), is(true));
        assertThat(glob.matches("Starts with"), is(false));

        glob.setPatternSource("Selfcontaining * is ?");
        assertThat(glob.matches("Selfcontaining whatsoever is !"), is(true));
        assertThat(glob.matches("Selfcontaining whatsoever is is is is is !"), is(true));
        assertThat(glob.matches("Selfcontaining whatsoever is "), is(false));
    }

    @Test
    public void testCaseSensitivitytMatching() throws Exception {
        Glob glob = new Glob("Only*String.", true);
        assertThat(glob.matches("Only a string."), is(true));
        assertThat(glob.matches("only SOMETHING String."), is(true));

        glob = new Glob("*teST", true);
        assertThat(glob.matches("whatsoever TEst"), is(true));

        glob = new Glob("teST", true);
        assertThat(glob.matches("TEst"), is(true));
    }

    @Test
    public void testContainsGlob() throws Exception {
        assertThat(Glob.containsGlob("*@optivo.de"), is(true));
        assertThat(Glob.containsGlob("this*@op?tivo.de"), is(true));
        assertThat(Glob.containsGlob("?@optivo.de"), is(true));
        assertThat(Glob.containsGlob("test@optivo.de"), is(false));
    }
}
