package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;

public class SetDestroyOnRebootVDSCommand extends VdsBrokerCommand<VdsAndVmIDVDSParametersBase> {
    public SetDestroyOnRebootVDSCommand(VdsAndVmIDVDSParametersBase parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().setDestroyOnReboot(getParameters().getVmId().toString());
        proceedProxyReturnValue();
    }
}
