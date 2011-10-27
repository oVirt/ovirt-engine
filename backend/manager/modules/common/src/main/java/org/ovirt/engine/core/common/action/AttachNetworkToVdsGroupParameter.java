package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AttachNetworkToVdsGroupParameter")
public class AttachNetworkToVdsGroupParameter extends VdsGroupOperationParameters {
    private static final long serialVersionUID = -2874549285727269806L;
    @XmlElement(name = "Network")
    private network _network;

    public AttachNetworkToVdsGroupParameter(VDSGroup group, network net) {
        super(group);
        _network = net;
    }

    public network getNetwork() {
        return _network;
    }

    public AttachNetworkToVdsGroupParameter() {
    }
}
