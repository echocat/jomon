/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.runtime.codec;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class OstermillerMd5UnitTest {

    @Test
    public void update() throws Exception {
        final DefaultMd5 dMd5 = new DefaultMd5();
        final OstermillerMd5 oMd5 = new OstermillerMd5();
        dMd5.update("foo");
        dMd5.update("bar");

        oMd5.update("foo");
        oMd5.update("bar");

        assertThat(oMd5.asHexString(), is(dMd5.asHexString()));
    }

}