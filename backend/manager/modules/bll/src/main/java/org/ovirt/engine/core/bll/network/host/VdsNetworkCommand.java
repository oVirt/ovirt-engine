package org.ovirt.engine.core.bll.network.host;

import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsParameters;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;

@SuppressWarnings("serial")
@CustomLogFields("NetworkName")
public abstract class VdsNetworkCommand<T extends AttachNetworkToVdsParameters> extends VdsCommand<T> {
    public VdsNetworkCommand(T parameters) {
        super(parameters);
    }

    public String getNetworkName() {
        return getParameters().getNetwork().getName();
    }
}
