package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;

public abstract class InfoVdsBrokerCommand<P extends VdsIdVDSCommandParametersBase> extends VdsBrokerCommand<P> {
    protected InfoVdsBrokerCommand(P parameters, VDS vds) {
        super(parameters, vds);
    }

    protected VDSInfoReturn infoReturn;

    @Override
    protected Status getReturnStatus() {
        return infoReturn.status;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return infoReturn;
    }
}
