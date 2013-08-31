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

import org.echocat.jomon.maven.TestPom.NameOfPomFileIs;
import org.echocat.jomon.testing.environments.LogEnvironment;
import org.apache.maven.model.Dependency;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.echocat.jomon.testing.BaseMatchers.is;
import static org.junit.Assert.assertThat;

public class MavenProjectWithModulesFactoryUnitTest {

    @Rule
    public final LogEnvironment _logEnvironment = new LogEnvironment();
    @Rule
    public final TestPom _pom = new TestPom();

    @Test
    @NameOfPomFileIs("poms/simple/pom.xml")
    public void testSimplePom() throws Exception {
        final MavenProjectWithModulesFactory factory = createFactory();
        final MavenProjectWithModules project = factory.createFor(createMavenEnvironment(), _pom.getFile());

        assertThat(project, hasId("org.echocat.jomon.maven.structure.test:simple:666"));
        assertThat(project, hasName("Simple Test"));

        final List<Dependency> dependencies = project.getProject().getDependencies();
        assertThat(dependencies.size(), is(1));
        final Dependency dependency1 = dependencies.iterator().next();
        assertThat(dependency1.getArtifactId(), is("commons-io"));
    }

    @Test
    @NameOfPomFileIs({"poms/structure/pom.xml", "poms/structure/child_a/pom.xml", "poms/structure/child_a/child_a_a/pom.xml", "poms/structure/child_a/child_a_b/pom.xml", "poms/structure/child_b/pom.xml", "poms/structure/child_b/child_b_a/pom.xml", "poms/structure/child_b/child_b_b/pom.xml"})
    public void testParentPom() throws Exception {
        final MavenProjectWithModulesFactory factory = createFactory();
        final MavenProjectWithModules parent = factory.createFor(createMavenEnvironment(), _pom.getFile());
        assertThat(parent, hasId("org.echocat.jomon.maven.structure.test:parent:666"));
        final List<MavenProjectWithModules> childrenOfParent = getSortedModulesOf(parent);
        assertThat(childrenOfParent.size(), is(2));

        final MavenProjectWithModules childA = childrenOfParent.get(0);
        assertThat(childA, hasId("org.echocat.jomon.maven.structure.test:child_a:667"));
        final List<MavenProjectWithModules> childrenOfChildA = getSortedModulesOf(childA);
        assertThat(childrenOfChildA.size(), is(2));
        assertThat(childrenOfChildA.get(0), hasId("org.echocat.jomon.maven.structure.test:child_a_a:667"));
        assertThat(childrenOfChildA.get(1), hasId("org.echocat.jomon.maven.structure.test:child_a_b:667"));

        final MavenProjectWithModules childB = childrenOfParent.get(1);
        assertThat(childB, hasId("org.echocat.jomon.maven.structure.test:child_b:668"));
        final List<MavenProjectWithModules> childrenOfChildB = getSortedModulesOf(childB);
        assertThat(childrenOfChildB.size(), is(2));
        assertThat(childrenOfChildB.get(0), hasId("org.echocat.jomon.maven.structure.test:child_b_a:668"));
        assertThat(childrenOfChildB.get(1), hasId("org.echocat.jomon.maven.structure.test:child_b_b:668"));
    }

    @Nonnull
    private Matcher<MavenProjectWithModules> hasId(@Nonnull final String artifactId) {
        return new TypeSafeMatcher<MavenProjectWithModules>() {
            @Override
            public boolean matchesSafely(MavenProjectWithModules item) {
                return item != null && item.getProject() != null && artifactId.equals(item.getProject().getGroupId() + ":" + item.getProject().getArtifactId() + ":" + item.getProject().getVersion());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has id ").appendValue(artifactId);
            }
        };
    }

    @Nonnull
    private Matcher<MavenProjectWithModules> hasName(@Nonnull final String name) {
        return new TypeSafeMatcher<MavenProjectWithModules>() {
            @Override
            public boolean matchesSafely(MavenProjectWithModules item) {
                return item != null && item.getProject() != null && name.equals(item.getProject().getName());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has name ").appendValue(name);
            }
        };
    }

    private List<MavenProjectWithModules> getSortedModulesOf(@Nonnull MavenProjectWithModules from) {
        final List<MavenProjectWithModules> modules = new ArrayList<>(from.getModules());
        Collections.sort(modules, new Comparator<MavenProjectWithModules>() {
            @Override
            public int compare(MavenProjectWithModules o1, MavenProjectWithModules o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        return modules;
    }

    @Nonnull
    private MavenProjectWithModulesFactory createFactory() throws Exception {
        return new MavenProjectWithModulesFactory();
    }

    @Nonnull
    private MavenEnvironment createMavenEnvironment() throws Exception {
        final MavenEnvironmentFactory environmentFactory = new MavenEnvironmentFactory();
        return environmentFactory.create();
    }

}
