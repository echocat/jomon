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

package org.echocat.jomon.runtime.valuemodule.testbeans.a.a.a;

import org.echocat.jomon.runtime.valuemodule.ValueModule.IsModularizedBy;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.a.a.BeanAAAModule.IsModule;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.a.a.a.BeanAAAA;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.a.a.b.BeanAAAB;
import org.echocat.jomon.runtime.valuemodule.testbeans.a.a.a.c.BeanAAAC;

import static org.echocat.jomon.runtime.valuemodule.testbeans.a.a.a.BeanAAAModule.a;
import static org.echocat.jomon.runtime.valuemodule.testbeans.a.a.a.BeanAAAModule.b;

@SuppressWarnings("InstanceVariableNamingConvention")
@IsModularizedBy(BeanAAAModule.class)
public class BeanAAA {

    private BeanAAAA _a;
    private BeanAAAB _b;
    private BeanAAAC _c;
    private String _d;

    @IsModule(a)
    public BeanAAAA getA() {
        return _a;
    }

    public void setA(BeanAAAA a) {
        _a = a;
    }

    @IsModule(b)
    public BeanAAAB getB() {
        return _b;
    }

    public void setB(BeanAAAB b) {
        _b = b;
    }

    public BeanAAAC getC() {
        return _c;
    }

    public void setC(BeanAAAC c) {
        _c = c;
    }

    public String getD() {
        return _d;
    }

    public void setD(String d) {
        _d = d;
    }
}
