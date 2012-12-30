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

package org.echocat.jomon.runtime.valuemodule.testbeans.a;

import org.echocat.jomon.runtime.valuemodule.ValueModule.IsModularizedBy;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.BeanAModule.IsModule;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.a.BeanAA;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.b.BeanAB;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.c.BeanAC;

@SuppressWarnings("InstanceVariableNamingConvention")
@IsModularizedBy(BeanAModule.class)
public class BeanA {

    private BeanAA _a;
    private BeanAB _b;
    private BeanAC _c;
    private String _d;

    @IsModule(BeanAModule.a)
    public BeanAA getA() {
        return _a;
    }

    public void setA(BeanAA a) {
        _a = a;
    }

    @IsModule(BeanAModule.b)
    public BeanAB getB() {
        return _b;
    }

    public void setB(BeanAB b) {
        _b = b;
    }

    public BeanAC getC() {
        return _c;
    }

    public void setC(BeanAC c) {
        _c = c;
    }

    public String getD() {
        return _d;
    }

    public void setD(String d) {
        _d = d;
    }
}
