package org.ovirt.engine.core.bll.pm;

import org.ovirt.engine.core.common.action.VdcReturnValueBase;

public class RestartVdsReturnValue extends VdcReturnValueBase {

    private boolean skippedDueToFencingPolicy;

    public boolean isSkippedDueToFencingPolicy() {
        return skippedDueToFencingPolicy;
    }

    public void setSkippedDueToFencingPolicy(boolean skippedDueToFencingPolicy) {
        this.skippedDueToFencingPolicy = skippedDueToFencingPolicy;
    }

}
