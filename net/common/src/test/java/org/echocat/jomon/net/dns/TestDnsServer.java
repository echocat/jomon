/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.net.dns;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

class TestDnsServer extends TemporaryDnsServer implements TestRule {

    @Override
    public Statement apply(final Statement base, Description description) { return new Statement() { @Override public void evaluate() throws Throwable {
        try {
            base.evaluate();
        } finally {
            try {
                close();
            } catch (Exception ignored) {}
        }
    }};}
}
