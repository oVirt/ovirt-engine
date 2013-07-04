package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class InstallVdsParameters extends VdsOperationActionParameters {

    private static final long serialVersionUID = 5066290843683399113L;

    private boolean privateIsReinstallOrUpgrade;
    private String privateoVirtIsoFile;


    public InstallVdsParameters() {
    }

    public InstallVdsParameters(Guid vdsId) {
        super();
        this.setVdsId(vdsId);
    }

    public InstallVdsParameters(Guid vdsId, String password) {
        super();
        this.setVdsId(vdsId);
        setPassword(password);
    }

    public boolean getIsReinstallOrUpgrade() {
        return privateIsReinstallOrUpgrade;
    }

    public String getoVirtIsoFile() {
        return privateoVirtIsoFile;
    }

    public void setIsReinstallOrUpgrade(boolean value) {
        privateIsReinstallOrUpgrade = value;
    }

    public void setoVirtIsoFile(String value) {
        privateoVirtIsoFile = value;
    }


}
