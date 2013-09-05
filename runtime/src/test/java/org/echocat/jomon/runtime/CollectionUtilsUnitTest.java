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

package org.echocat.jomon.runtime;

import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.multimap.MultiHashMap;
import org.junit.Test;

import java.util.Collections;

import static java.util.Arrays.asList;
import static org.echocat.jomon.runtime.CollectionUtils.asMap;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


public class CollectionUtilsUnitTest {
    @Test
    public void testIsNotEmpty() throws Exception {
        // Test multi map ...
        final MultiMap<String, String> multiMap = new MultiHashMap<>();
        assertThat(CollectionUtils.isNotEmpty(multiMap), is(false));
        multiMap.put("Key", "Value");
        assertThat(CollectionUtils.isNotEmpty(multiMap), is(true));
        // Test null values (incl. the MultiMap) ...
        assertThat(CollectionUtils.isNotEmpty(Collections.singletonMap("Testkey", asList("Testvalue")).get("NonexistentKey")), is(false));
        assertThat(CollectionUtils.isNotEmpty(Collections.singletonMap("Testkey", asMap("Testkey", "Testvalue")).get("NonexistentKey")), is(false));
        assertThat(CollectionUtils.isNotEmpty(Collections.singletonMap("Testkey", asMap("Testkey", "Testvalue")).get("NonexistentKey")), is(false));
        assertThat(CollectionUtils.isNotEmpty(Collections.singletonMap("Testkey", multiMap).get("NonexistentKey")), is(false));
        // Test empty values ...
        assertThat(CollectionUtils.isNotEmpty(Collections.emptyList()), is(false));
        assertThat(CollectionUtils.isNotEmpty(Collections.emptyMap()), is(false));
        // Test non empty values ...
        assertThat(CollectionUtils.isNotEmpty(Collections.singletonList("Test")), is(true));
        assertThat(CollectionUtils.isNotEmpty(Collections.singletonMap("Testkey", "Testvalue")), is(true));
    }
}
