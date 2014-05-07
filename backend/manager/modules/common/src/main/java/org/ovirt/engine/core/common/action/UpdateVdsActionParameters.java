package org.ovirt.engine.core.common.action;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.FenceAgent;
import org.ovirt.engine.core.common.businessentities.VdsStatic;

public class UpdateVdsActionParameters extends VdsOperationActionParameters {
    private static final long serialVersionUID = -7467029979089285065L;
    private List<FenceAgent> fenceAgents;

    public UpdateVdsActionParameters(VdsStatic vdsStatic, String password, boolean installVds) {
        super(vdsStatic, password);
        _installVds = installVds;
    }

    public UpdateVdsActionParameters(VdsStatic vdsStatic, boolean installVds) {
        super(vdsStatic);
        _installVds = installVds;
    }

    private boolean _installVds;

    public boolean getInstallVds() {
        return _installVds;
    }

    public void setInstallVds(boolean value) {
        _installVds = value;
    }

    private boolean privateIsReinstallOrUpgrade;

    public boolean getIsReinstallOrUpgrade() {
        return privateIsReinstallOrUpgrade;
    }

    public void setIsReinstallOrUpgrade(boolean value) {
        privateIsReinstallOrUpgrade = value;
    }

    private String privateoVirtIsoFile;

    public String getoVirtIsoFile() {
        return privateoVirtIsoFile;
    }

    public void setoVirtIsoFile(String value) {
        privateoVirtIsoFile = value;
    }

    public UpdateVdsActionParameters() {
    }

    public List<FenceAgent> getFenceAgents() {
        return fenceAgents;
    }

    public void setFenceAgents(List<FenceAgent> fenceAgents) {
        this.fenceAgents = fenceAgents;
    }
}
