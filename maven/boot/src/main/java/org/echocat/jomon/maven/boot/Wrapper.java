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

package org.echocat.jomon.maven.boot;

import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

import static org.tanukisoftware.wrapper.WrapperManager.setConsoleTitle;

public class Wrapper implements WrapperListener {

    @Override
    public Integer start(String[] args) {
        try {
            MavenBoot.main(args);
        } catch (final Exception e) {
            throw new RuntimeException("Fatal error in main thread.", e);
        }
        return null;
    }

    public static void main(String[] args) {
        setConsoleTitle("MavenBoot");
        WrapperManager.start(new Wrapper(), args);
    }

    @Override public int stop(int exitCode) { return exitCode; }
    @Override public void controlEvent(int event) {}
}
