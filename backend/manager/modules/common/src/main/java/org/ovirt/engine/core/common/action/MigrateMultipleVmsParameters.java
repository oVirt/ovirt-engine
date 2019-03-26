package org.ovirt.engine.core.common.action;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public class MigrateMultipleVmsParameters extends ActionParametersBase {
    private static final long serialVersionUID = 4760846259790814498L;

    private List<Guid> vms;
    private boolean forceMigration;
    private String reason;
    private boolean canIgnoreHardVmAffinity;

    private List<Guid> hostBlackList = Collections.emptyList();
    private Guid destinationHostId;

    private boolean addVmsInPositiveHardAffinity;

    public MigrateMultipleVmsParameters() {
    }

    public MigrateMultipleVmsParameters(List<Guid> vms, boolean forceMigration) {
        this.vms = vms;
        this.forceMigration = forceMigration;
    }

    public List<Guid> getVms() {
        return vms;
    }

    public void setVms(List<Guid> vms) {
        this.vms = vms;
    }

    public boolean isForceMigration() {
        return forceMigration;
    }

    public void setForceMigration(boolean forceMigration) {
        this.forceMigration = forceMigration;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isCanIgnoreHardVmAffinity() {
        return canIgnoreHardVmAffinity;
    }

    public void setCanIgnoreHardVmAffinity(boolean canIgnoreHardVmAffinity) {
        this.canIgnoreHardVmAffinity = canIgnoreHardVmAffinity;
    }

    public List<Guid> getHostBlackList() {
        return hostBlackList;
    }

    public void setHostBlackList(List<Guid> hostBlackList) {
        this.hostBlackList = hostBlackList;
    }

    public Guid getDestinationHostId() {
        return destinationHostId;
    }

    public void setDestinationHostId(Guid destinationHostId) {
        this.destinationHostId = destinationHostId;
    }

    public boolean isAddVmsInPositiveHardAffinity() {
        return addVmsInPositiveHardAffinity;
    }

    public void setAddVmsInPositiveHardAffinity(boolean addVmsInPositiveHardAffinity) {
        this.addVmsInPositiveHardAffinity = addVmsInPositiveHardAffinity;
    }
}
