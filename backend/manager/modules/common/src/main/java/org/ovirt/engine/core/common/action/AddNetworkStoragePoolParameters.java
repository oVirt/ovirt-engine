package org.ovirt.engine.core.common.action;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.compat.Guid;

public class AddNetworkStoragePoolParameters extends StoragePoolParametersBase {
    private static final long serialVersionUID = -7392121807419409051L;

    @Valid
    @NotNull
    private Network network;

    private boolean vnicProfileRequired;

    public AddNetworkStoragePoolParameters() {
        vnicProfileRequired = true;
    }

    public AddNetworkStoragePoolParameters(Guid storagePoolId, Network network) {
        super(storagePoolId);
        this.network = network;
        vnicProfileRequired = true;
    }

    public Network getNetwork() {
        return network;
    }

    public boolean isVnicProfileRequired() {
        return vnicProfileRequired;
    }

    public void setVnicProfileRequired(boolean vnicProfileRequired) {
        this.vnicProfileRequired = vnicProfileRequired;
    }
}
