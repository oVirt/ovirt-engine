package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;

public class AttachNetworkToVdsGroupParameter extends VdsGroupOperationParameters {
    private static final long serialVersionUID = -2874549285727269806L;
    private Network _network;

    public AttachNetworkToVdsGroupParameter(VDSGroup group, Network net) {
        super(group);
        _network = net;
    }

    public Network getNetwork() {
        return _network;
    }

    public AttachNetworkToVdsGroupParameter() {
    }
}
