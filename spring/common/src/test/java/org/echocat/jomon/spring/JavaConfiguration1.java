/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.spring;

import org.echocat.jomon.spring.beans.Bean1;
import org.echocat.jomon.spring.beans.Bean2;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({BaseConfiguration.class})
public class JavaConfiguration1 {

    @Bean(autowire = Autowire.BY_TYPE)
    public Bean1 bean1() {
        return new Bean1();
    }

    @Bean(autowire = Autowire.BY_TYPE)
    public Bean2 bean2() {
        return new Bean2();
    }

}
