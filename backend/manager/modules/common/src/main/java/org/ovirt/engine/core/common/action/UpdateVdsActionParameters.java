package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.VdsStatic;

public class UpdateVdsActionParameters extends VdsOperationActionParameters {
    private static final long serialVersionUID = -7467029979089285065L;

    public UpdateVdsActionParameters(VdsStatic vdsStatic, String rootPassword, boolean installVds) {
        super(vdsStatic, rootPassword);
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
}
