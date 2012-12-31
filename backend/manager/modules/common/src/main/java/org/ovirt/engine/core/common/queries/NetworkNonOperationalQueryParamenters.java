package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.businessentities.network.Network;

public class NetworkNonOperationalQueryParamenters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -5386574130657761284L;

    public NetworkNonOperationalQueryParamenters(Guid vdsgroupid, Network net) {
        _vdsgroupid = vdsgroupid;
        _network = net;
    }

    private Guid _vdsgroupid;

    public Guid getVdsGroupId() {
        return _vdsgroupid;
    }

    private Network _network;

    public Network getNetwork() {
        return _network;
    }

    public NetworkNonOperationalQueryParamenters() {
    }
}
