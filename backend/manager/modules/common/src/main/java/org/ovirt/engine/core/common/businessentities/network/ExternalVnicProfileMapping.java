package org.ovirt.engine.core.common.businessentities.network;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class ExternalVnicProfileMapping implements Serializable {

    private String externalNetworkName;
    private String externalNetworkProfileName;
    private Guid vnicProfileId;

    private ExternalVnicProfileMapping() {}

    public ExternalVnicProfileMapping(String externalNetworkName,
            String externalNetworkProfileName,
            Guid vnicProfileId) {
        this.externalNetworkName = externalNetworkName;
        this.externalNetworkProfileName = externalNetworkProfileName;
        this.vnicProfileId = vnicProfileId;
    }

    public String getExternalNetworkName() {
        return externalNetworkName;
    }

    public String getExternalNetworkProfileName() {
        return externalNetworkProfileName;
    }

    public Guid getVnicProfileId() {
        return vnicProfileId;
    }
}
