package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "NetworkNonOperationalQueryParamenters")
public class NetworkNonOperationalQueryParamenters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -5386574130657761284L;

    public NetworkNonOperationalQueryParamenters(Guid vdsgroupid, network net) {
        _vdsgroupid = vdsgroupid;
        _network = net;
    }

    @XmlElement(name = "Vdsgroupid")
    private Guid _vdsgroupid;

    public Guid getVdsGroupId() {
        return _vdsgroupid;
    }

    @XmlElement(name = "Network")
    private network _network;

    public network getNetwork() {
        return _network;
    }

    public NetworkNonOperationalQueryParamenters() {
    }
}
