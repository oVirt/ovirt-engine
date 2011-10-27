package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "DisplayNetworkToVdsGroupParameters")
public class DisplayNetworkToVdsGroupParameters extends VdsGroupOperationParameters {
    private static final long serialVersionUID = 6552130939864906665L;

    @XmlElement(name = "Network")
    private network _network;

    @XmlElement
    private boolean _is_display;

    public DisplayNetworkToVdsGroupParameters(VDSGroup group, network net, boolean is_display) {
        super(group);
        _network = net;
        _is_display = is_display;
    }

    public network getNetwork() {
        return _network;
    }

    public boolean getIsDisplay() {
        return _is_display;
    }

    public DisplayNetworkToVdsGroupParameters() {
    }
}
