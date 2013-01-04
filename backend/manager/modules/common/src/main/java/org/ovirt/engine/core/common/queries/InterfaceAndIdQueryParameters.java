package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.network.NetworkInterface;
import org.ovirt.engine.core.compat.Guid;

public class InterfaceAndIdQueryParameters extends IdQueryParameters {
    private static final long serialVersionUID = 2875732930025180055L;

    private NetworkInterface<?> iface;

    public InterfaceAndIdQueryParameters() {
    }

    public InterfaceAndIdQueryParameters(Guid vdsId, NetworkInterface<?> iface) {
        super(vdsId);
        this.iface = iface;
    }

    public NetworkInterface<?> getInterface() {
        return iface;
    }
}
