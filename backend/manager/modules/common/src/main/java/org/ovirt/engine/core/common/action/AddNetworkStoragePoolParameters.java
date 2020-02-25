package org.ovirt.engine.core.common.action;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.compat.Guid;

public class AddNetworkStoragePoolParameters extends StoragePoolParametersBase {
    private static final long serialVersionUID = -7392121807419409051L;

    @Valid
    @NotNull
    private Network network;

    private boolean vnicProfileRequired;

    private List<NetworkCluster> networkClusterList;

    private boolean vnicProfilePublicUse;

    private boolean async;

    public AddNetworkStoragePoolParameters() {
        vnicProfileRequired = true;
        async = true;
    }

    public AddNetworkStoragePoolParameters(Guid storagePoolId, Network network) {
        super(storagePoolId);
        this.network = network;
        vnicProfileRequired = true;
        async = true;
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

    public void setNetworkClusterList(List<NetworkCluster> networkClusterList) {
        this.networkClusterList = networkClusterList;
    }

    public List<NetworkCluster> getNetworkClusterList() {
        return networkClusterList;
    }

    public boolean isVnicProfilePublicUse() {
        return vnicProfilePublicUse;
    }

    public void setVnicProfilePublicUse(boolean vnicProfilePublicUse) {
        this.vnicProfilePublicUse = vnicProfilePublicUse;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }
}
