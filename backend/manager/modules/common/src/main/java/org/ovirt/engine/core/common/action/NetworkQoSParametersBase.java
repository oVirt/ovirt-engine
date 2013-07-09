package org.ovirt.engine.core.common.action;


import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.compat.Guid;

import javax.validation.Valid;

public class NetworkQoSParametersBase extends VdcActionParametersBase {

    private static final long serialVersionUID = 1304387921254873524L;

    @Valid
    private NetworkQoS networkQoS;
    private Guid networkQoSGuid;

    public NetworkQoS getNetworkQoS() {
        return networkQoS;
    }

    public void setNetworkQoS(NetworkQoS networkQoS) {
        this.networkQoS = networkQoS;
    }

    public Guid getNetworkQoSGuid() {
        return networkQoSGuid;
    }

    public void setNetworkQoSGuid(Guid networkQoSGuid) {
        this.networkQoSGuid = networkQoSGuid;
    }
}
