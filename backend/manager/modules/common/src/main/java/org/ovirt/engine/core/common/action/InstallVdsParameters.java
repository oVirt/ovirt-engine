package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class InstallVdsParameters extends VdsActionParameters {
    private static final long serialVersionUID = 5066290843683399113L;

    public InstallVdsParameters(Guid vdsId, String password) {
        super(vdsId);
        setRootPassword(password);
    }

    private String privateRootPassword;

    public String getRootPassword() {
        return privateRootPassword;
    }

    private void setRootPassword(String value) {
        privateRootPassword = value;
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

    private boolean overrideFirewall;

    public void setOverrideFirewall(boolean overrideFirewall) {
        this.overrideFirewall = overrideFirewall;
    }

    public boolean getOverrideFirewall() {
        return overrideFirewall;
    }

    public InstallVdsParameters() {
    }
}
