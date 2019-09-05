package org.ovirt.engine.core.common.action;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.compat.Guid;

public class AddVmParameters extends VmManagementParametersBase {
    private static final long serialVersionUID = 8641610721114989096L;

    private Guid diskOperatorAuthzPrincipalDbId;
    private Guid poolId;
    private boolean useCollapse;
    private Map<Guid, Guid> srcDiskIdToTargetDiskIdMapping = new HashMap<>();
    private Map<Guid, Guid> srcVmNicIdToTargetVmNicIdMapping = new HashMap<>();

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

    public boolean isUseCollapse() {
        return useCollapse;
    }

    public void setUseCollapse(boolean useCollapse) {
        this.useCollapse = useCollapse;
    }

    public Map<Guid, Guid> getSrcDiskIdToTargetDiskIdMapping() {
        return srcDiskIdToTargetDiskIdMapping;
    }

    public void setSrcDiskIdToTargetDiskIdMapping(Map<Guid, Guid> srcDiskIdToTargetDiskIdMapping) {
        this.srcDiskIdToTargetDiskIdMapping = srcDiskIdToTargetDiskIdMapping;
    }

    public Map<Guid, Guid> getSrcVmNicIdToTargetVmNicIdMapping() {
        return srcVmNicIdToTargetVmNicIdMapping;
    }

    public void setSrcVmNicIdToTargetVmNicIdMapping(Map<Guid, Guid> srcVmNicIdToTargetVmNicIdMapping) {
        this.srcVmNicIdToTargetVmNicIdMapping = srcVmNicIdToTargetVmNicIdMapping;
    }
}
