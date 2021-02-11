package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.compat.Guid;

public class ClusterOperationParameters extends ClusterParametersBase {
    private static final long serialVersionUID = -457715512828262834L;
    @Valid
    private Cluster cluster;
    private boolean internalCommand;
    private boolean forceResetEmulatedMachine = false;
    private boolean changeVmsChipsetToQ35 = false;
    private Guid managementNetworkId;

    public ClusterOperationParameters(Cluster group) {
        super(group.getId());
        cluster = group;
    }

    public ClusterOperationParameters(Cluster cluster, Guid managementNetworkId) {
        super(cluster.getId());
        this.cluster = cluster;
        this.managementNetworkId = managementNetworkId;
    }

    public ClusterOperationParameters() {
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public boolean isInternalCommand() {
        return internalCommand;
    }

    public void setIsInternalCommand(boolean value) {
        internalCommand = value;
    }

    public void setForceResetEmulatedMachine(boolean isResetEmulatedMachine) {
        this.forceResetEmulatedMachine = isResetEmulatedMachine;
    }

    public boolean isForceResetEmulatedMachine() {
        return this.forceResetEmulatedMachine;
    }

    public void setChangeVmsChipsetToQ35(boolean editVmsBiosType) {
        this.changeVmsChipsetToQ35 = editVmsBiosType;
    }

    public boolean isChangeVmsChipsetToQ35() {
        return changeVmsChipsetToQ35;
    }

    public Guid getManagementNetworkId() {
        return managementNetworkId;
    }

    public void setManagementNetworkId(Guid managementNetworkId) {
        this.managementNetworkId = managementNetworkId;
    }
}
