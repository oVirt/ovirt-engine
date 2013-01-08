package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.compat.Guid;

public class AddNetworkStoragePoolParameters extends StoragePoolParametersBase {
    private static final long serialVersionUID = -7392121807419409051L;

    @Valid
    private Network network;

    private boolean publicUse;

    public AddNetworkStoragePoolParameters() {
    }

    public AddNetworkStoragePoolParameters(Guid storagePoolId, Network network) {
        super(storagePoolId);
        this.network = network;
    }

    public Network getNetwork() {
        return network;
    }

    public void setPublicUse(boolean publicUse) {
        this.publicUse = publicUse;
    }

    public boolean isPublicUse() {
        return publicUse;
    }
}
