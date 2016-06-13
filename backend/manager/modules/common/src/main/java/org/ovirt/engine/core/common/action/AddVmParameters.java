package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.compat.Guid;

public class AddVmParameters extends VmManagementParametersBase {
    private static final long serialVersionUID = 8641610721114989096L;

    private Guid diskOperatorAuthzPrincipalDbId;
    private Guid poolId;

    public AddVmParameters() {
    }

    public AddVmParameters(VmStatic vmStatic) {
        super(vmStatic);
    }

    public AddVmParameters(VM vm) {
        this(vm.getStaticData());
    }

    public Guid getDiskOperatorAuthzPrincipalDbId() {
        return diskOperatorAuthzPrincipalDbId;
    }

    public void setDiskOperatorAuthzPrincipalDbId(Guid diskOperatorAuthzPrincipalDbId) {
        this.diskOperatorAuthzPrincipalDbId = diskOperatorAuthzPrincipalDbId;
    }

    public Guid getPoolId() {
        return poolId;
    }

    public void setPoolId(Guid poolId) {
        this.poolId = poolId;
    }

}
