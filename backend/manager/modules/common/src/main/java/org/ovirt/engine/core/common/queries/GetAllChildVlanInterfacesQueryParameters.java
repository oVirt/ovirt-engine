package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.network.NetworkInterface;
import org.ovirt.engine.core.compat.Guid;

public class GetAllChildVlanInterfacesQueryParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 2875732930025180055L;

    public GetAllChildVlanInterfacesQueryParameters(Guid vdsId, NetworkInterface<?> iface) {
        _vdsId = vdsId;
        _interface = iface;
    }

    private Guid _vdsId;

    public Guid getVdsId() {
        return _vdsId;
    }

    private NetworkInterface<?> _interface;

    public NetworkInterface<?> getInterface() {
        return _interface;
    }

    public GetAllChildVlanInterfacesQueryParameters() {
    }
}
