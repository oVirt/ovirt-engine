package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class InstallVdsParameters extends VdsActionParameters {

    private static final long serialVersionUID = 5066290843683399113L;

    private String privateRootPassword;
    private boolean privateIsReinstallOrUpgrade;
    private String privateoVirtIsoFile;
    private boolean overrideFirewall;

    public InstallVdsParameters() {
    }

    public InstallVdsParameters(Guid vdsId, String password) {
        super(vdsId);
        setRootPassword(password);
    }

    public boolean getIsReinstallOrUpgrade() {
        return privateIsReinstallOrUpgrade;
    }

    public boolean getOverrideFirewall() {
        return overrideFirewall;
    }

    public String getoVirtIsoFile() {
        return privateoVirtIsoFile;
    }

    public String getRootPassword() {
        return privateRootPassword;
    }

    public void setIsReinstallOrUpgrade(boolean value) {
        privateIsReinstallOrUpgrade = value;
    }

    public void setOverrideFirewall(boolean overrideFirewall) {
        this.overrideFirewall = overrideFirewall;
    }

    public void setoVirtIsoFile(String value) {
        privateoVirtIsoFile = value;
    }

    private void setRootPassword(String value) {
        privateRootPassword = value;
    }
}
