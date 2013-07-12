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

    public AddNetworkStoragePoolParameters() {
    }

    public AddNetworkStoragePoolParameters(Guid storagePoolId, Network network) {
        super(storagePoolId);
        this.network = network;
    }

    public Network getNetwork() {
        return network;
    }
}
