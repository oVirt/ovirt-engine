package org.ovirt.engine.core.common.action;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.compat.Guid;

public class AddExternalSubnetParameters extends ExternalSubnetParameters {

    private static final long serialVersionUID = 1065284644633155727L;

    @NotNull
    private Guid providerId;

    @NotNull
    private String networkId;

    public AddExternalSubnetParameters() {
    }

    public AddExternalSubnetParameters(ExternalSubnet subnet, Guid providerId, String networkId) {
        super(subnet);
        this.providerId = providerId;
        this.networkId = networkId;
    }

    public Guid getProviderId() {
        return providerId;
    }

    public void setProviderId(Guid providerId) {
        this.providerId = providerId;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }
}
