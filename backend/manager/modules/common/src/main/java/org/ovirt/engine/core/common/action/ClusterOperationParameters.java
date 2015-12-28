package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.Cluster;

public class ClusterOperationParameters extends ClusterParametersBase {
    private static final long serialVersionUID = -2184123302248929010L;
    @Valid
    private Cluster cluster;
    private boolean forceResetEmulatedMachine = false;

    public ClusterOperationParameters(Cluster group) {
        super(group.getId());
        cluster = group;
    }

    public Cluster getCluster() {
        return cluster;
    }

    private boolean privateIsInternalCommand;

    public boolean getIsInternalCommand() {
        return privateIsInternalCommand;
    }

    public void setIsInternalCommand(boolean value) {
        privateIsInternalCommand = value;
    }

    public ClusterOperationParameters() {
    }

    public void setForceResetEmulatedMachine(boolean isResetEmulatedMachine) {
        this.forceResetEmulatedMachine = isResetEmulatedMachine;
    }

    public boolean isForceResetEmulatedMachine() {
        return this.forceResetEmulatedMachine;
    }
}
