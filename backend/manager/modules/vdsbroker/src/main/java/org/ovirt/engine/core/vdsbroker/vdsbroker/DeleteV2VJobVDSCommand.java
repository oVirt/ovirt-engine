package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;

public class DeleteV2VJobVDSCommand<P extends VdsAndVmIDVDSParametersBase> extends VdsBrokerCommand<P> {

    public DeleteV2VJobVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().deleteV2VJob(getParameters().getVmId().toString());
        proceedProxyReturnValue();
    }
}
