package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;

public class CancelConvertVmVDSCommand<T extends VdsAndVmIDVDSParametersBase> extends VdsBrokerCommand<T> {

    public CancelConvertVmVDSCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().abortV2VJob(getParameters().getVmId().toString());
        proceedProxyReturnValue();
    }
}
