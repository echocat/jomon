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

package org.echocat.jomon.runtime.logging;

import javax.annotation.Nonnull;

import static org.echocat.jomon.runtime.logging.Slf4jUtils.tryFixMdcInSlf4j;

public class Log4J2BasedLoggingEnvironmentGenerator implements LoggingEnvironmentGenerator {

    @Override
    public boolean supports(@Nonnull LoggingEnvironmentConfiguration configuration) {
        return configuration instanceof Log4J2BasedLoggingEnvironmentConfiguration;
    }

    @Nonnull
    @Override
    public LoggingEnvironment generate(@Nonnull LoggingEnvironmentConfiguration requirement) {
        if (requirement instanceof Log4J2BasedLoggingEnvironmentConfiguration) {
            return generate((Log4J2BasedLoggingEnvironmentConfiguration) requirement);
        } else {
            throw new IllegalArgumentException("Could not handle " + requirement + ".");
        }
    }

    @Nonnull
    protected LoggingEnvironment generate(@Nonnull Log4J2BasedLoggingEnvironmentConfiguration requirement) {
        if (requirement.isInstallSl4jRequired()) {
            tryFixMdcInSlf4j();
        }
        final Log4J2BasedLoggingEnvironment result = new Log4J2BasedLoggingEnvironment();
        result.init(requirement);
        return result;
    }

}
