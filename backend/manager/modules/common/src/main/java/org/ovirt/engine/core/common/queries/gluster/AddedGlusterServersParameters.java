package org.ovirt.engine.core.common.queries.gluster;

import org.ovirt.engine.core.compat.Guid;

/**
 * This will be used by added gluster servers query command. <br>
 */
public class AddedGlusterServersParameters extends GlusterParameters {

    private boolean isServerPublicKeyRequired;

    public AddedGlusterServersParameters() {
    }

    public AddedGlusterServersParameters(Guid clusterId, boolean isServerPublicKeyRequired) {
        super(clusterId);
        setServerPublicKeyRequired(isServerPublicKeyRequired);
    }

    public boolean isServerPublicKeyRequired() {
        return isServerPublicKeyRequired;
    }

    public void setServerPublicKeyRequired(boolean isServerPublicKeyRequired) {
        this.isServerPublicKeyRequired = isServerPublicKeyRequired;
    }

}
