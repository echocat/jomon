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

package org.echocat.jomon.runtime.valuemodule.testbeans.a.a;

import org.echocat.jomon.runtime.valuemodule.ValueModule.IsModularizedBy;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.a.BeanAAModule.IsModule;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.a.a.BeanAAA;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.a.b.BeanAAB;

import static org.echocat.jomon.runtime.valuemodule.testbeans.a.a.BeanAAModule.a;
import static org.echocat.jomon.runtime.valuemodule.testbeans.a.a.BeanAAModule.b;

@SuppressWarnings("InstanceVariableNamingConvention")
@IsModularizedBy(BeanAAModule.class)
public class BeanAA {

    private BeanAAA _a;
    private BeanAAB _b;
    private String _c;


    @IsModule(a)
    public BeanAAA getA() {
        return _a;
    }

    public void setA(BeanAAA a) {
        _a = a;
    }

    @IsModule(b)
    public BeanAAB getB() {
        return _b;
    }

    public void setB(BeanAAB b) {
        _b = b;
    }

    public String getC() {
        return _c;
    }

    public void setC(String c) {
        _c = c;
    }
}
