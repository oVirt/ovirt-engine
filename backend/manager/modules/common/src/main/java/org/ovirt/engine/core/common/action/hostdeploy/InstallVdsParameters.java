package org.ovirt.engine.core.common.action.hostdeploy;

import org.ovirt.engine.core.common.action.VdsOperationActionParameters;
import org.ovirt.engine.core.compat.Guid;

public class InstallVdsParameters extends VdsOperationActionParameters {

    private static final long serialVersionUID = 5066290843683399113L;

    private boolean reinstallOrUpgrade;
    private String oVirtIsoFile;

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
        return reinstallOrUpgrade;
    }

    public String getoVirtIsoFile() {
        return oVirtIsoFile;
    }

    public void setIsReinstallOrUpgrade(boolean value) {
        reinstallOrUpgrade = value;
    }

    public void setoVirtIsoFile(String value) {
        oVirtIsoFile = value;
    }
}
