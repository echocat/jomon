package org.echocat.jomon.process.daemon;

import javax.annotation.Nonnull;

public class ApplicationDaemonQuery extends BaseApplicationDaemonQuery<ApplicationDaemonQuery, ApplicationDaemon<?>> {

    @Nonnull
    public static ApplicationDaemonQuery applicationDaemon() {
        return new ApplicationDaemonQuery();
    }

}
