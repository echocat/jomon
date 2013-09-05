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

package org.echocat.jomon.maven;

import org.echocat.jomon.testing.environments.LogEnvironment;
import org.junit.Rule;
import org.junit.Test;

import static org.echocat.jomon.testing.BaseMatchers.hasItems;
import static org.echocat.jomon.testing.BaseMatchers.isNotNull;
import static org.junit.Assert.assertThat;

public class MavenEnvironmentFactoryUnitTest {

    @Rule
    public final LogEnvironment _logEnvironment = new LogEnvironment();

    @Test
    public void testCreate() throws Exception {
        final MavenEnvironmentFactory factory = new MavenEnvironmentFactory();
        final MavenEnvironment environment = factory.create();
        assertThat(environment, isNotNull());
        assertThat(environment.getRequest(), isNotNull());
        assertThat(environment.getRepositorySystemSession(), isNotNull());
        assertThat(environment.getRequest().getLocalRepository(), isNotNull());
        assertThat(environment.getRequest().getRemoteRepositories(), hasItems());
    }
}
