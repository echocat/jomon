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

package org.echocat.jomon.format.label;

import static java.util.Locale.ENGLISH;

public abstract class LabelSupport implements Label {

    @Override
    public String toString() {
        String result;
        try {
            result = toLocalized(ENGLISH);
        } catch (final Exception ignored) {
            result = "<unknown>";
        }
        return result;
    }

}
