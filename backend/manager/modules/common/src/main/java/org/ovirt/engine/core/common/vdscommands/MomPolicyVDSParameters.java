package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.VDS;

/**
 * Parameters used to fine tune MoM policy
 */
public class MomPolicyVDSParameters extends VdsIdVDSCommandParametersBase {
    private boolean enableBalloon;
    private boolean enableKsm;

    public MomPolicyVDSParameters(VDS vds, boolean enableBalloon, boolean enableKsm) {
        super(vds.getId());
        this.enableBalloon = enableBalloon;
        this.enableKsm = enableKsm;
    }

    public MomPolicyVDSParameters() {
    }

    public boolean isEnableBalloon() {
        return enableBalloon;
    }

    public void setEnableBalloon(boolean enableBalloon) {
        this.enableBalloon = enableBalloon;
    }

    public boolean isEnableKsm() {
        return enableKsm;
    }

    public void setEnableKsm(boolean enableKsm) {
        this.enableKsm = enableKsm;
    }
}
