package org.echocat.jomon.runtime.util;

import javax.annotation.Nullable;

public interface IdEnabled<ID> {

    @Nullable
    public ID getId();

}
