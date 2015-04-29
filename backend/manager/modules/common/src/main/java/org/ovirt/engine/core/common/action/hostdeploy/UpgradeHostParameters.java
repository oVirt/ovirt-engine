package org.ovirt.engine.core.common.action.hostdeploy;

import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.compat.Guid;

public class UpgradeHostParameters extends VdsActionParameters {

    private static final long serialVersionUID = 3142278762372841895L;
    private VDSStatus initialStatus;
    private String oVirtIsoFile;

    public UpgradeHostParameters(Guid hostId) {
        super(hostId);
    }

    public UpgradeHostParameters() {
    }

    public VDSStatus getInitialStatus() {
        return initialStatus;
    }

    public void setInitialStatus(VDSStatus initialStatus) {
        this.initialStatus = initialStatus;
    }

    public String getoVirtIsoFile() {
        return oVirtIsoFile;
    }

    public void setoVirtIsoFile(String oVirtIsoFile) {
        this.oVirtIsoFile = oVirtIsoFile;
    }
}
