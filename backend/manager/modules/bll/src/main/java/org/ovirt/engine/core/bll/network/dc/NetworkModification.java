package org.ovirt.engine.core.bll.network.dc;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.businessentities.network.Network;

public abstract class NetworkModification<T extends AddNetworkStoragePoolParameters> extends NetworkCommon<T> {

    public NetworkModification(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setStoragePoolId(getNetwork().getDataCenterId());
    }

    @Override
    protected Network getNetwork() {
        return getParameters().getNetwork();
    }
}
