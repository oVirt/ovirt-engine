package org.ovirt.engine.core.common.action;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.compat.Guid;

public class AddExternalSubnetParameters extends ExternalSubnetParameters {

    private static final long serialVersionUID = 1065284644633155727L;

    @NotNull
    private Guid networkId;

    public AddExternalSubnetParameters() {
    }

    public AddExternalSubnetParameters(ExternalSubnet subnet, Guid networkId) {
        super(subnet);
        this.networkId = networkId;
    }

    public Guid getNetworkId() {
        return networkId;
    }

    public void setNetworkId(Guid networkId) {
        this.networkId = networkId;
    }

}
