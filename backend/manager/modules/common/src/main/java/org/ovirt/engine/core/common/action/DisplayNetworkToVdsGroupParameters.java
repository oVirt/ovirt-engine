package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.businessentities.network.Network;

public class DisplayNetworkToVdsGroupParameters extends VdsGroupOperationParameters {
    private static final long serialVersionUID = 6552130939864906665L;

    private Network _network;

    private boolean _is_display;

    public DisplayNetworkToVdsGroupParameters(VDSGroup group, Network net, boolean is_display) {
        super(group);
        _network = net;
        _is_display = is_display;
    }

    public Network getNetwork() {
        return _network;
    }

    public boolean getIsDisplay() {
        return _is_display;
    }

    public DisplayNetworkToVdsGroupParameters() {
    }
}
