package org.ovirt.engine.core.common.action.hostdeploy;

import org.ovirt.engine.core.compat.Guid;

public class ApproveVdsParameters extends InstallVdsParameters {
    private static final long serialVersionUID = -48762426583568791L;
    private boolean isApprovedByRegister;

    public ApproveVdsParameters(Guid vdsId) {
        super(vdsId);
    }

    public ApproveVdsParameters() {
    }

    public void setApprovedByRegister(boolean isAutoApprove) {
        this.isApprovedByRegister = isAutoApprove;
    }

    public boolean isApprovedByRegister() {
        return isApprovedByRegister;
    }
}
