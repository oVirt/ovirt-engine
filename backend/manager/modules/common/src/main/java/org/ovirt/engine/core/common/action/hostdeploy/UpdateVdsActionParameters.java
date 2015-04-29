package org.ovirt.engine.core.common.action.hostdeploy;

import java.util.List;

import org.ovirt.engine.core.common.action.VdsOperationActionParameters;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;

public class UpdateVdsActionParameters extends VdsOperationActionParameters {
    private static final long serialVersionUID = -7467029979089285065L;
    private List<FenceAgent> fenceAgents;
    private boolean installHost;
    private boolean reinstallOrUpgrade;
    private String oVirtIsoFile;

    /**
     * This field is intended for internal use to pass the value of the host prior to its upgrade to the callback
     */
    private VDSStatus initialStatus;

    public UpdateVdsActionParameters() {
    }

    public UpdateVdsActionParameters(VdsStatic vdsStatic, boolean installHost) {
        super(vdsStatic);
        this.installHost = installHost;
    }

    public UpdateVdsActionParameters(VdsStatic vdsStatic, String password, boolean installHost) {
        super(vdsStatic, password);
        this.installHost = installHost;
    }

    public boolean isInstallHost() {
        return installHost;
    }

    public void setInstallHost(boolean installHost) {
        this.installHost = installHost;
    }

    public boolean isReinstallOrUpgrade() {
        return reinstallOrUpgrade;
    }

    public void setReinstallOrUpgrade(boolean reinstallOrUpgrade) {
        this.reinstallOrUpgrade = reinstallOrUpgrade;
    }

    public String getoVirtIsoFile() {
        return oVirtIsoFile;
    }

    public void setoVirtIsoFile(String oVirtIsoFile) {
        this.oVirtIsoFile = oVirtIsoFile;
    }


    public List<FenceAgent> getFenceAgents() {
        return fenceAgents;
    }

    public void setFenceAgents(List<FenceAgent> fenceAgents) {
        this.fenceAgents = fenceAgents;
    }

    public VDSStatus getInitialStatus() {
        return initialStatus;
    }

    public void setInitialStatus(VDSStatus initialStatus) {
        this.initialStatus = initialStatus;
    }
}
