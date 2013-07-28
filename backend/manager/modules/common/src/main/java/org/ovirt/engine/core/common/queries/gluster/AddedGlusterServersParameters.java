package org.ovirt.engine.core.common.queries.gluster;

import org.ovirt.engine.core.compat.Guid;

/**
 * Parameter class with cluster Id and isFingerPrintRequired as parameters. <br>
 * This will be used by added gluster servers query command. <br>
 */
public class AddedGlusterServersParameters extends GlusterParameters {

    private boolean isServerKeyFingerprintRequired;

    public AddedGlusterServersParameters() {
    }

    public AddedGlusterServersParameters(Guid clusterId, boolean isServerKeyFingerprintRequired) {
        super(clusterId);
        setServerKeyFingerprintRequired(isServerKeyFingerprintRequired);
    }

    public boolean isServerKeyFingerprintRequired() {
        return isServerKeyFingerprintRequired;
    }

    public void setServerKeyFingerprintRequired(boolean isServerKeyFingerprintRequired) {
        this.isServerKeyFingerprintRequired = isServerKeyFingerprintRequired;
    }

}
