package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.compat.Guid;

public class AddNetworkWithSubnetParameters extends AddNetworkStoragePoolParameters {
    private static final long serialVersionUID = -7392121823419409051L;

    private ExternalSubnet externalSubnet;

    public AddNetworkWithSubnetParameters() {
    }

    public AddNetworkWithSubnetParameters(Guid storagePoolId, Network network) {
        super(storagePoolId, network);

    }

    public ExternalSubnet getExternalSubnet() {
        return externalSubnet;
    }

    public void setExternalSubnet(ExternalSubnet externalSubnet) {
        this.externalSubnet = externalSubnet;
    }
}
