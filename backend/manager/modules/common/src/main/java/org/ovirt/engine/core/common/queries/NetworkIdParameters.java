package org.ovirt.engine.core.common.queries;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.compat.Guid;

/**
 * The Parameters class for queries that use only Network Id
 */
public class NetworkIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -5550670672065955312L;

    @NotNull
    private Guid networkId;

    public NetworkIdParameters() {
    }

    public NetworkIdParameters(Guid networkId) {
        this.networkId = networkId;
    }

    public Guid getNetworkId() {
        return networkId;
    }
}
