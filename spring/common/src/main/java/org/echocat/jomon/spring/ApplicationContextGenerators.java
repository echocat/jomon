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

package org.echocat.jomon.spring;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.ServiceLoader.load;
import static org.echocat.jomon.runtime.CollectionUtils.addAll;
import static org.echocat.jomon.runtime.CollectionUtils.asImmutableList;

public class ApplicationContextGenerators {

    @Nonnull
    private static final Iterable<ApplicationContextGenerator> ALL = loadAll();
    @Nonnull
    private static final ApplicationContextGenerator DEFAULT = loadDefaultOf(ALL);

    private ApplicationContextGenerators() {}

    @Nonnull
    public static ApplicationContextGenerator getDefault() {
        return DEFAULT;
    }

    @Nonnull
    public static ApplicationContextGenerator applicationContextGenerator() {
        return getDefault();
    }

    @Nonnull
    public static Iterable<ApplicationContextGenerator> getAll() {
        return ALL;
    }

    @Nonnull
    private static Iterable<ApplicationContextGenerator> loadAll() {
        final List<ApplicationContextGenerator> result = new ArrayList<>();
        addAll(result, load(ApplicationContextGenerator.class));
        result.add(DefaultApplicationContextGenerator.getInstance());
        return asImmutableList(result);
    }

    @Nonnull
    private static ApplicationContextGenerator loadDefaultOf(Iterable<ApplicationContextGenerator> applicationGenerators) {
        final Iterator<ApplicationContextGenerator> i = applicationGenerators.iterator();
        return i.next();
    }

}
