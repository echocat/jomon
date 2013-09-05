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

import org.echocat.jomon.runtime.util.Entry;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.*;

import static com.google.common.collect.Iterators.asEnumeration;
import static org.echocat.jomon.runtime.CollectionUtils.asMap;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ResourceBundlesUnitTest {

    private static final Locale LOCALE_A_WITH_LANGUAGE_COUNTRY_AND_VARIANT = new Locale("language", "country", "variant");
    private static final Locale LOCALE_B_WITH_LANGUAGE_AND_COUNTRY = new Locale("language", "country");
    private static final Locale LOCALE_C_WITH_LANGUAGE = new Locale("language");
    private static final Locale LOCALE_D_WHICH_IS_BLANK = new Locale("");

    private static final ResourceBundle BUNDLE_A = createBundleWithValuesOf("1", "a");
    private static final ResourceBundle BUNDLE_B = createBundleWithValuesOf("1", "b", "2", "b");
    private static final ResourceBundle BUNDLE_C = createBundleWithValuesOf("1", "c", "2", "c", "3", "c");
    private static final ResourceBundle BUNDLE_D = createBundleWithValuesOf("1", "d", "2", "d", "3", "d", "4", "d");

    private static final ResourceBundles BUNDLES = createTestResourceBundles();

    @Test
    public void testGetBundleWithExistingBundles() throws Exception {
        assertThat(BUNDLES.getBundle(LOCALE_A_WITH_LANGUAGE_COUNTRY_AND_VARIANT), containsBundles(BUNDLE_A, BUNDLE_B, BUNDLE_C, BUNDLE_D));
        assertThat(BUNDLES.getBundle(LOCALE_B_WITH_LANGUAGE_AND_COUNTRY), containsBundles(BUNDLE_B, BUNDLE_C, BUNDLE_D));
        assertThat(BUNDLES.getBundle(LOCALE_C_WITH_LANGUAGE), containsBundles(BUNDLE_C, BUNDLE_D));
        assertThat(BUNDLES.getBundle(LOCALE_D_WHICH_IS_BLANK), containsBundles(BUNDLE_D));
    }

    protected static Matcher<ResourceBundle> containsBundles(final ResourceBundle... resourceBundles) {
        return new TypeSafeMatcher<ResourceBundle>() {
            @Override
            public boolean matchesSafely(ResourceBundle item) {
                final boolean result;
                if (item instanceof CombinedResourceBundle) {
                    final CombinedResourceBundle combinedBundle = (CombinedResourceBundle) item;
                    result = Arrays.asList(resourceBundles).equals(combinedBundle.getResourceBundles());
                } else {
                    result = false;
                }
                return result;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("contains " + Arrays.toString(resourceBundles));
            }
        };
    }

    @Test
    public void testGetBundleWithMissingBundles() throws Exception {
        try {
            BUNDLES.getBundle(new Locale("foo"));
        } catch (NoSuchElementException expected) {}
        try {
            BUNDLES.getBundle(new Locale("bar"));
        } catch (NoSuchElementException expected) {}
    }

    @Test
    public void testContainsBundleWithExistingBundles() throws Exception {
        assertThat(BUNDLES.containsBundle(LOCALE_A_WITH_LANGUAGE_COUNTRY_AND_VARIANT), is(true));
        assertThat(BUNDLES.containsBundle(LOCALE_B_WITH_LANGUAGE_AND_COUNTRY), is(true));
        assertThat(BUNDLES.containsBundle(LOCALE_C_WITH_LANGUAGE), is(true));
        assertThat(BUNDLES.containsBundle(LOCALE_D_WHICH_IS_BLANK), is(true));
    }

    @Test
    public void testContainsBundleWithMissingBundles() throws Exception {
        assertThat(BUNDLES.containsBundle(new Locale("foo")), is(false));
        assertThat(BUNDLES.containsBundle(new Locale("bar")), is(false));
    }

    @Test
    public void testGetWithExistingKeys() throws Exception {
        assertThat(BUNDLES.get(LOCALE_A_WITH_LANGUAGE_COUNTRY_AND_VARIANT, "1"), is("a"));
        assertThat(BUNDLES.get(LOCALE_A_WITH_LANGUAGE_COUNTRY_AND_VARIANT, "2"), is("b"));
        assertThat(BUNDLES.get(LOCALE_A_WITH_LANGUAGE_COUNTRY_AND_VARIANT, "3"), is("c"));
        assertThat(BUNDLES.get(LOCALE_A_WITH_LANGUAGE_COUNTRY_AND_VARIANT, "4"), is("d"));

        assertThat(BUNDLES.get(LOCALE_B_WITH_LANGUAGE_AND_COUNTRY, "1"), is("b"));
        assertThat(BUNDLES.get(LOCALE_B_WITH_LANGUAGE_AND_COUNTRY, "2"), is("b"));
        assertThat(BUNDLES.get(LOCALE_B_WITH_LANGUAGE_AND_COUNTRY, "3"), is("c"));
        assertThat(BUNDLES.get(LOCALE_B_WITH_LANGUAGE_AND_COUNTRY, "4"), is("d"));

        assertThat(BUNDLES.get(LOCALE_C_WITH_LANGUAGE, "1"), is("c"));
        assertThat(BUNDLES.get(LOCALE_C_WITH_LANGUAGE, "2"), is("c"));
        assertThat(BUNDLES.get(LOCALE_C_WITH_LANGUAGE, "3"), is("c"));
        assertThat(BUNDLES.get(LOCALE_C_WITH_LANGUAGE, "4"), is("d"));

        assertThat(BUNDLES.get(LOCALE_D_WHICH_IS_BLANK, "1"), is("d"));
        assertThat(BUNDLES.get(LOCALE_D_WHICH_IS_BLANK, "2"), is("d"));
        assertThat(BUNDLES.get(LOCALE_D_WHICH_IS_BLANK, "3"), is("d"));
        assertThat(BUNDLES.get(LOCALE_D_WHICH_IS_BLANK, "4"), is("d"));
    }

    @Test
    public void testGetWithMissingKeys() throws Exception {
        try {
            BUNDLES.get(LOCALE_A_WITH_LANGUAGE_COUNTRY_AND_VARIANT, "x");
            fail("Exception missing.");
        } catch (MissingResourceException expected) {}
        try {
            BUNDLES.get(LOCALE_B_WITH_LANGUAGE_AND_COUNTRY, "x");
            fail("Exception missing.");
        } catch (MissingResourceException expected) {}
        try {
            BUNDLES.get(LOCALE_C_WITH_LANGUAGE, "x");
            fail("Exception missing.");
        } catch (MissingResourceException expected) {}
        try {
            BUNDLES.get(LOCALE_D_WHICH_IS_BLANK, "x");
            fail("Exception missing.");
        } catch (MissingResourceException expected) {}
    }

    @Test
    public void testIterator() throws Exception {
        final Iterator<Entry<Locale,ResourceBundle>> i = BUNDLES.iterator();
        assertThat(i.hasNext(), is(true));
        assertThat(i.next(), isLocaleAndBundle(LOCALE_D_WHICH_IS_BLANK, BUNDLE_D));
        assertThat(i.next(), isLocaleAndBundle(LOCALE_C_WITH_LANGUAGE, BUNDLE_C));
        assertThat(i.next(), isLocaleAndBundle(LOCALE_B_WITH_LANGUAGE_AND_COUNTRY, BUNDLE_B));
        assertThat(i.next(), isLocaleAndBundle(LOCALE_A_WITH_LANGUAGE_COUNTRY_AND_VARIANT, BUNDLE_A));
    }

    @Nonnull
    private static Matcher<Entry<Locale, ResourceBundle>> isLocaleAndBundle(@Nonnull final Locale locale, @Nonnull final ResourceBundle bundle) { return new TypeSafeMatcher<Entry<Locale, ResourceBundle>>() {
        @Override
        public boolean matchesSafely(Entry<Locale, ResourceBundle> item) {
            final boolean result;
            if (item != null) {
                result = locale.equals(item.getKey()) && bundle.equals(item.getValue());
            } else {
                result = false;
            }
            return result;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("is locale ").appendValue(locale).appendText(" and bundle ").appendValue(bundle);
        }
    };}

    @Nonnull
    private static ResourceBundles createTestResourceBundles() {
        final ResourceBundles bundles = new ResourceBundles();
        bundles.putBundle(LOCALE_A_WITH_LANGUAGE_COUNTRY_AND_VARIANT, BUNDLE_A);
        bundles.putBundle(LOCALE_B_WITH_LANGUAGE_AND_COUNTRY, BUNDLE_B);
        bundles.putBundle(LOCALE_C_WITH_LANGUAGE, BUNDLE_C);
        bundles.putBundle(LOCALE_D_WHICH_IS_BLANK, BUNDLE_D);
        return bundles;
    }

    private static ResourceBundle createBundleWithValuesOf(@Nonnull String... values) {
        final Map<String, String> mapWithValues = asMap(values);
        return new ResourceBundle() {
            @Override protected Object handleGetObject(String key) { return mapWithValues.get(key); }
            @Override public Enumeration<String> getKeys() { return asEnumeration(mapWithValues.keySet().iterator()); }
        };
    }

}
