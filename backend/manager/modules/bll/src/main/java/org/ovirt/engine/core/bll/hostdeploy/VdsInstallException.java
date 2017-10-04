package org.ovirt.engine.core.bll.hostdeploy;

import org.ovirt.engine.core.common.businessentities.VDSStatus;

@SuppressWarnings("serial")
public class VdsInstallException extends RuntimeException {
    private VDSStatus status;

    public VdsInstallException(VDSStatus status, String message) {
        super(message);
        this.status = status;
    }

    public VdsInstallException(VDSStatus status, String message, Exception cause) {
        super(message, cause);
        this.status = status;
    }

    public VDSStatus getStatus() {
        return status;
    }
}

