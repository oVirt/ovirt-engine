package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;

public class VmPoolToAdGroupParameters extends AdGroupElementParametersBase {
    private static final long serialVersionUID = 5695955304480728659L;

    public VmPoolToAdGroupParameters(Guid vmPoolId, LdapGroup group, boolean isInternal) {
        super(group);
        setVmPoolId(vmPoolId);
        setIsInternal(isInternal);
    }

    private Guid privateVmPoolId;

    public Guid getVmPoolId() {
        return privateVmPoolId;
    }

    private void setVmPoolId(Guid value) {
        privateVmPoolId = value;
    }

    private boolean privateIsInternal;

    public boolean getIsInternal() {
        return privateIsInternal;
    }

    private void setIsInternal(boolean value) {
        privateIsInternal = value;
    }

    public VmPoolToAdGroupParameters() {
    }
}
