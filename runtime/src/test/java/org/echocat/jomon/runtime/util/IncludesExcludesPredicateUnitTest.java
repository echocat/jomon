/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2014 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.runtime.util;

import org.junit.Test;

import static org.echocat.jomon.runtime.util.IncludesExcludesPredicate.predicate;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class IncludesExcludesPredicateUnitTest {

    @Test
    public void noLimits() throws Exception {
        assertThat(predicate().apply("a"), is(true));
        assertThat(predicate().apply("b"), is(true));
    }

    @Test
    public void onlyIncludes() throws Exception {
        assertThat(predicate().including("a").apply("a"), is(true));
        assertThat(predicate().including("a").apply("b"), is(false));
    }

    @Test
    public void onlyExcludes() throws Exception {
        assertThat(predicate().excluding("b").apply("a"), is(true));
        assertThat(predicate().excluding("b").apply("b"), is(false));
    }

    @Test
    public void includesAndExcludes() throws Exception {
        assertThat(predicate().including("a", "b").excluding("b").apply("a"), is(true));
        assertThat(predicate().including("a", "b").excluding("b").apply("b"), is(false));
        assertThat(predicate().including("a", "b").excluding("b").apply("c"), is(false));
    }

}
