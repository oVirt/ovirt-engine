package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

public class InterfaceAndIdQueryParameters extends IdQueryParameters {
    private static final long serialVersionUID = 2875732930025180055L;

    private VdsNetworkInterface iface;

    public InterfaceAndIdQueryParameters() {
    }

    public InterfaceAndIdQueryParameters(Guid vdsId, VdsNetworkInterface iface) {
        super(vdsId);
        this.iface = iface;
    }

    public VdsNetworkInterface getInterface() {
        return iface;
    }
}
