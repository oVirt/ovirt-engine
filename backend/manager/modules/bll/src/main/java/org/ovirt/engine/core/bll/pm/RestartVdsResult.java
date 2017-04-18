package org.ovirt.engine.core.bll.pm;

import java.io.Serializable;

public class RestartVdsResult implements Serializable {

    private boolean skippedDueToFencingPolicy;

    public boolean isSkippedDueToFencingPolicy() {
        return skippedDueToFencingPolicy;
    }

    public void setSkippedDueToFencingPolicy(boolean skippedDueToFencingPolicy) {
        this.skippedDueToFencingPolicy = skippedDueToFencingPolicy;
    }

}
