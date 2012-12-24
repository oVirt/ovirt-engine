package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetVmsAndNetworkInterfacesByNetworkIdParameters extends NetworkIdParameters {

    private static final long serialVersionUID = -3306070454507733795L;

    public GetVmsAndNetworkInterfacesByNetworkIdParameters() {
    }

    /**
     * Create parameters to filter by the given network ID.
     *
     * @param networkId
     *            The network ID to filter the VMs for.
     */
    public GetVmsAndNetworkInterfacesByNetworkIdParameters(Guid networkId) {
        super(networkId);
    }

}
