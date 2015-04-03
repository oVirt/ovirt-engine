package org.ovirt.engine.core.common.action;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;

public class UpdateVdsActionParameters extends VdsOperationActionParameters {
    private static final long serialVersionUID = -7467029979089285065L;
    private List<FenceAgent> fenceAgents;
    private boolean installHost;
    private boolean reinstallOrUpgrade;
    private String oVirtIsoFile;

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
}
