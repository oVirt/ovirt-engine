package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;

import org.ovirt.engine.core.common.users.*;

public class VmPoolUserParameters extends VmPoolSimpleUserParameters implements java.io.Serializable {
    private static final long serialVersionUID = -5672324868972973061L;

    public VmPoolUserParameters(Guid vmPoolId, VdcUser user, boolean isInternal) {
        super(vmPoolId, user.getUserId());
        setVdcUserData(user);
        setIsInternal(isInternal);
    }

    private VdcUser privateVdcUserData;

    public VdcUser getVdcUserData() {
        return privateVdcUserData;
    }

    private void setVdcUserData(VdcUser value) {
        privateVdcUserData = value;
    }

    private boolean privateIsInternal;

    public boolean getIsInternal() {
        return privateIsInternal;
    }

    private void setIsInternal(boolean value) {
        privateIsInternal = value;
    }

    private Guid privateVmId = new Guid();

    public Guid getVmId() {
        return privateVmId;
    }

    public void setVmId(Guid value) {
        privateVmId = value;
    }

    public VmPoolUserParameters() {
    }
}
