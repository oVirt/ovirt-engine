package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;

public class NetworkNonOperationalQueryParamenters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -5386574130657761284L;

    public NetworkNonOperationalQueryParamenters(Guid vdsgroupid, network net) {
        _vdsgroupid = vdsgroupid;
        _network = net;
    }

    private Guid _vdsgroupid;

    public Guid getVdsGroupId() {
        return _vdsgroupid;
    }

    private network _network;

    public network getNetwork() {
        return _network;
    }

    public NetworkNonOperationalQueryParamenters() {
    }
}
