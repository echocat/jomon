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

public class ApplicationGenerators {

    @Nonnull
    private static final Iterable<ApplicationGenerator> ALL = loadAll();
    @Nonnull
    private static final ApplicationGenerator DEFAULT = loadDefaultOf(ALL);

    private ApplicationGenerators() {}

    @Nonnull
    public static ApplicationGenerator getDefault() {
        return DEFAULT;
    }

    @Nonnull
    public static ApplicationGenerator applicationGenerator() {
        return getDefault();
    }

    @Nonnull
    public static Iterable<ApplicationGenerator> getAll() {
        return ALL;
    }

    @Nonnull
    private static Iterable<ApplicationGenerator> loadAll() {
        final List<ApplicationGenerator> result = new ArrayList<>();
        addAll(result, load(ApplicationGenerator.class));
        result.add(DefaultApplicationGenerator.getInstance());
        return asImmutableList(result);
    }

    @Nonnull
    private static ApplicationGenerator loadDefaultOf(Iterable<ApplicationGenerator> applicationGenerators) {
        final Iterator<ApplicationGenerator> i = applicationGenerators.iterator();
        return i.next();
    }

}
