package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;

public class VmToAdGroupParameters extends AdGroupElementParametersBase {
    private static final long serialVersionUID = -3998069506955069055L;
    private Guid _vmId = new Guid();

    public VmToAdGroupParameters(Guid vmId, LdapGroup adGroup) {
        super(adGroup);
        _vmId = vmId;
    }

    public Guid getVmId() {
        return _vmId;
    }

    public VmToAdGroupParameters() {
    }
}
