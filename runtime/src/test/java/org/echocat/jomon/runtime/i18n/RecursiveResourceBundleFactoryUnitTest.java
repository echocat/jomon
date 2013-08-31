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

package org.echocat.jomon.runtime.i18n;

import org.echocat.jomon.runtime.i18n.testpackage.subpackage.TestClass;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RecursiveResourceBundleFactoryUnitTest {

    private static final Locale BLANK = new Locale("");
    private static final Locale DE = new Locale("de");
    private static final Locale DE_DE = new Locale("de", "DE");
    private static final Locale DE_DE_FOO = new Locale("de", "DE", "foo");
    private static final Locale EN = new Locale("en");
    private static final Locale EN_GB = new Locale("en", "GB");
    private static final Locale EN_GB_FOO = new Locale("en", "GB", "foo");
    private static final Locale EN_US = new Locale("en", "US");

    private final RecursiveResourceBundleFactory _factory = createFactory();

    private final ResourceBundle _bundleForBlank = createTestBundleFor(_factory, BLANK);
    private final ResourceBundle _bundleForDe = createTestBundleFor(_factory, DE);
    private final ResourceBundle _bundleForDeDe = createTestBundleFor(_factory, DE_DE);
    private final ResourceBundle _bundleForDeDeFoo = createTestBundleFor(_factory, DE_DE_FOO);
    private final ResourceBundle _bundleForEn = createTestBundleFor(_factory, EN);
    private final ResourceBundle _bundleForEnGb = createTestBundleFor(_factory, EN_GB);
    private final ResourceBundle _bundleForEnGbFoo = createTestBundleFor(_factory, EN_GB_FOO);
    private final ResourceBundle _bundleForEnUs = createTestBundleFor(_factory, EN_US);

    @Test
    public void testTestBundleBlank() throws Exception {
        assertThat(_bundleForBlank.getString("a1"), is("class_blank"));
        assertThat(_bundleForBlank.getString("a2"), is("class_blank"));
        assertThat(_bundleForBlank.getString("a3"), is("class_blank"));
        assertThat(_bundleForBlank.getString("a4"), is("class_blank"));

        assertThat(_bundleForBlank.getString("b1"), is("sub_blank"));
        assertThat(_bundleForBlank.getString("b2"), is("sub_blank"));
        assertThat(_bundleForBlank.getString("b3"), is("sub_blank"));
        assertThat(_bundleForBlank.getString("b4"), is("sub_blank"));

        assertThat(_bundleForBlank.getString("c1"), is("base_blank"));
        assertThat(_bundleForBlank.getString("c2"), is("base_blank"));
        assertThat(_bundleForBlank.getString("c3"), is("base_blank"));
        assertThat(_bundleForBlank.getString("c4"), is("base_blank"));

        assertThat(_bundleForBlank.getString("x1"), is("baseclass_blank"));
        assertThat(_bundleForBlank.getString("x2"), is("baseclass_blank"));
        assertThat(_bundleForBlank.getString("x3"), is("baseclass_blank"));
        assertThat(_bundleForBlank.getString("x4"), is("baseclass_blank"));
    }

    @Test
    public void testTestBundleDe() throws Exception {
        assertThat(_bundleForDe.getString("a1"), is("class_de"));
        assertThat(_bundleForDe.getString("a2"), is("class_de"));
        assertThat(_bundleForDe.getString("a3"), is("class_de"));
        assertThat(_bundleForDe.containsKey("a4"), is(false));

        assertThat(_bundleForDe.getString("b1"), is("sub_de"));
        assertThat(_bundleForDe.getString("b2"), is("sub_de"));
        assertThat(_bundleForDe.getString("b3"), is("sub_de"));
        assertThat(_bundleForDe.containsKey("b4"), is(false));

        assertThat(_bundleForDe.getString("c1"), is("base_de"));
        assertThat(_bundleForDe.getString("c2"), is("base_de"));
        assertThat(_bundleForDe.getString("c3"), is("base_de"));
        assertThat(_bundleForDe.containsKey("c4"), is(false));

        assertThat(_bundleForDe.getString("x1"), is("baseclass_de"));
        assertThat(_bundleForDe.getString("x2"), is("baseclass_de"));
        assertThat(_bundleForDe.getString("x3"), is("baseclass_de"));
        assertThat(_bundleForDe.containsKey("x4"), is(false));
    }

    @Test
    public void testTestBundleDeDe() throws Exception {
        assertThat(_bundleForDeDe.getString("a1"), is("class_de_DE"));
        assertThat(_bundleForDeDe.getString("a2"), is("class_de_DE"));
        assertThat(_bundleForDeDe.containsKey("a3"), is(false));
        assertThat(_bundleForDeDe.containsKey("a4"), is(false));

        assertThat(_bundleForDeDe.getString("b1"), is("sub_de_DE"));
        assertThat(_bundleForDeDe.getString("b2"), is("sub_de_DE"));
        assertThat(_bundleForDeDe.containsKey("b3"), is(false));
        assertThat(_bundleForDeDe.containsKey("b4"), is(false));

        assertThat(_bundleForDeDe.getString("c1"), is("base_de_DE"));
        assertThat(_bundleForDeDe.getString("c2"), is("base_de_DE"));
        assertThat(_bundleForDeDe.containsKey("c3"), is(false));
        assertThat(_bundleForDeDe.containsKey("c4"), is(false));

        assertThat(_bundleForDeDe.getString("x1"), is("baseclass_de_DE"));
        assertThat(_bundleForDeDe.getString("x2"), is("baseclass_de_DE"));
        assertThat(_bundleForDeDe.containsKey("x3"), is(false));
        assertThat(_bundleForDeDe.containsKey("x4"), is(false));
    }

    @Test
    public void testTestBundleDeDeFoo() throws Exception {
        assertThat(_bundleForDeDeFoo.getString("a1"), is("class_de_DE-foo"));
        assertThat(_bundleForDeDeFoo.containsKey("a2"), is(false));
        assertThat(_bundleForDeDeFoo.containsKey("a3"), is(false));
        assertThat(_bundleForDeDeFoo.containsKey("a4"), is(false));

        assertThat(_bundleForDeDeFoo.getString("b1"), is("sub_de_DE-foo"));
        assertThat(_bundleForDeDeFoo.containsKey("b2"), is(false));
        assertThat(_bundleForDeDeFoo.containsKey("b3"), is(false));
        assertThat(_bundleForDeDeFoo.containsKey("b4"), is(false));

        assertThat(_bundleForDeDeFoo.getString("c1"), is("base_de_DE-foo"));
        assertThat(_bundleForDeDeFoo.containsKey("c2"), is(false));
        assertThat(_bundleForDeDeFoo.containsKey("c3"), is(false));
        assertThat(_bundleForDeDeFoo.containsKey("c4"), is(false));

        assertThat(_bundleForDeDeFoo.getString("x1"), is("baseclass_de_DE-foo"));
        assertThat(_bundleForDeDeFoo.containsKey("x2"), is(false));
        assertThat(_bundleForDeDeFoo.containsKey("x3"), is(false));
        assertThat(_bundleForDeDeFoo.containsKey("x4"), is(false));
    }

    @Test
    public void testTestBundleEn() throws Exception {
        assertThat(_bundleForEn.getString("a1"), is("class_en"));
        assertThat(_bundleForEn.getString("a2"), is("class_en"));
        assertThat(_bundleForEn.getString("a3"), is("class_en"));
        assertThat(_bundleForEn.containsKey("a4"), is(false));

        assertThat(_bundleForEn.getString("b1"), is("sub_en"));
        assertThat(_bundleForEn.getString("b2"), is("sub_en"));
        assertThat(_bundleForEn.getString("b3"), is("sub_en"));
        assertThat(_bundleForEn.containsKey("b4"), is(false));

        assertThat(_bundleForEn.getString("c1"), is("base_en"));
        assertThat(_bundleForEn.getString("c2"), is("base_en"));
        assertThat(_bundleForEn.getString("c3"), is("base_en"));
        assertThat(_bundleForEn.containsKey("c4"), is(false));

        assertThat(_bundleForEn.getString("x1"), is("baseclass_en"));
        assertThat(_bundleForEn.getString("x2"), is("baseclass_en"));
        assertThat(_bundleForEn.getString("x3"), is("baseclass_en"));
        assertThat(_bundleForEn.containsKey("x4"), is(false));
    }

    @Test
    public void testTestBundleEnGb() throws Exception {
        assertThat(_bundleForEnGb.getString("a1"), is("class_en_GB"));
        assertThat(_bundleForEnGb.getString("a2"), is("class_en_GB"));
        assertThat(_bundleForEnGb.containsKey("a3"), is(false));
        assertThat(_bundleForEnGb.containsKey("a4"), is(false));

        assertThat(_bundleForEnGb.getString("b1"), is("sub_en_GB"));
        assertThat(_bundleForEnGb.getString("b2"), is("sub_en_GB"));
        assertThat(_bundleForEnGb.containsKey("b3"), is(false));
        assertThat(_bundleForEnGb.containsKey("b4"), is(false));

        assertThat(_bundleForEnGb.getString("c1"), is("base_en_GB"));
        assertThat(_bundleForEnGb.getString("c2"), is("base_en_GB"));
        assertThat(_bundleForEnGb.containsKey("c3"), is(false));
        assertThat(_bundleForEnGb.containsKey("c4"), is(false));

        assertThat(_bundleForEnGb.getString("x1"), is("baseclass_en_GB"));
        assertThat(_bundleForEnGb.getString("x2"), is("baseclass_en_GB"));
        assertThat(_bundleForEnGb.containsKey("x3"), is(false));
        assertThat(_bundleForEnGb.containsKey("x4"), is(false));
    }

    @Test
    public void testTestBundleEnGbFoo() throws Exception {
        assertThat(_bundleForEnGbFoo.getString("a1"), is("class_en_GB-foo"));
        assertThat(_bundleForEnGbFoo.containsKey("a2"), is(false));
        assertThat(_bundleForEnGbFoo.containsKey("a3"), is(false));
        assertThat(_bundleForEnGbFoo.containsKey("a4"), is(false));

        assertThat(_bundleForEnGbFoo.getString("b1"), is("sub_en_GB-foo"));
        assertThat(_bundleForEnGbFoo.containsKey("b2"), is(false));
        assertThat(_bundleForEnGbFoo.containsKey("b3"), is(false));
        assertThat(_bundleForEnGbFoo.containsKey("b4"), is(false));

        assertThat(_bundleForEnGbFoo.getString("c1"), is("base_en_GB-foo"));
        assertThat(_bundleForEnGbFoo.containsKey("c2"), is(false));
        assertThat(_bundleForEnGbFoo.containsKey("c3"), is(false));
        assertThat(_bundleForEnGbFoo.containsKey("c4"), is(false));

        assertThat(_bundleForEnGbFoo.getString("x1"), is("baseclass_en_GB-foo"));
        assertThat(_bundleForEnGbFoo.containsKey("x2"), is(false));
        assertThat(_bundleForEnGbFoo.containsKey("x3"), is(false));
        assertThat(_bundleForEnGbFoo.containsKey("x4"), is(false));
    }

    @Test
    public void testTestBundleEnUs() throws Exception {
        assertThat(_bundleForEnUs.getString("a1"), is("class_en_US"));
        assertThat(_bundleForEnUs.getString("a2"), is("class_en_US"));
        assertThat(_bundleForEnUs.containsKey("a3"), is(false));
        assertThat(_bundleForEnUs.containsKey("a4"), is(false));

        assertThat(_bundleForEnUs.getString("b1"), is("sub_en_US"));
        assertThat(_bundleForEnUs.getString("b2"), is("sub_en_US"));
        assertThat(_bundleForEnUs.containsKey("b3"), is(false));
        assertThat(_bundleForEnUs.containsKey("b4"), is(false));

        assertThat(_bundleForEnUs.getString("c1"), is("base_en_US"));
        assertThat(_bundleForEnUs.getString("c2"), is("base_en_US"));
        assertThat(_bundleForEnUs.containsKey("c3"), is(false));
        assertThat(_bundleForEnUs.containsKey("c4"), is(false));

        assertThat(_bundleForEnUs.getString("x1"), is("baseclass_en_US"));
        assertThat(_bundleForEnUs.getString("x2"), is("baseclass_en_US"));
        assertThat(_bundleForEnUs.containsKey("x3"), is(false));
        assertThat(_bundleForEnUs.containsKey("x4"), is(false));
    }

    @Nonnull
    private static RecursiveResourceBundleFactory createFactory() {
        final RecursiveResourceBundleFactory factory = new RecursiveResourceBundleFactory();
        return factory;
    }

    private static ResourceBundle createTestBundleFor(@Nonnull RecursiveResourceBundleFactory factory, @Nonnull Locale locale) {
        return createBundleFor(factory, TestClass.class, locale);
    }

    private static ResourceBundle createBundleFor(@Nonnull RecursiveResourceBundleFactory factory, @Nonnull Class<?> type, @Nonnull Locale locale) {
        return factory.getFor(type, locale);
    }

}
