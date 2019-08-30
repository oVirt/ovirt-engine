package org.ovirt.engine.core.common.action.hostdeploy;

import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.compat.Guid;

public class UpgradeHostParameters extends VdsActionParameters {

    private static final long serialVersionUID = 3142278762372841895L;
    private VDSStatus initialStatus;
    private String oVirtIsoFile;
    private boolean reboot;
    private int timeout;

    public UpgradeHostParameters(Guid hostId) {
        this(hostId, true);
    }

    public UpgradeHostParameters(Guid hostId, boolean reboot) {
        super(hostId);
        this.reboot = reboot;
    }

    public UpgradeHostParameters(Guid hostId, boolean reboot, int timeout) {
        this(hostId, reboot);
        this.timeout = timeout;
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

    public boolean isReboot() {
        return reboot;
    }

    public void setReboot(boolean reboot) {
        this.reboot = reboot;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
