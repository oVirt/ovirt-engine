package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.VDS;

/**
 * Parameters used to fine tune MoM policy
 */
public class MomPolicyVDSParameters extends VdsIdVDSCommandParametersBase {
    private boolean enableBalloon;
    private boolean enableKsm;
    private boolean ksmMergeAcrossNumaNodes;

    public MomPolicyVDSParameters() {
    }

    public MomPolicyVDSParameters(VDS vds,
            boolean enableBallooning,
            boolean enableKsm,
            boolean ksmMergeAcrossNumaNodes) {
        super(vds.getId());
        this.enableBalloon = enableBallooning;
        this.enableKsm = enableKsm;
        this.ksmMergeAcrossNumaNodes = ksmMergeAcrossNumaNodes;
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

    public boolean isKsmMergeAcrossNumaNodes() {
        return ksmMergeAcrossNumaNodes;
    }

    public void setKsmMergeAcrossNumaNodes(boolean ksmMergeAcrossNumaNodes) {
        this.ksmMergeAcrossNumaNodes = ksmMergeAcrossNumaNodes;
    }
}
