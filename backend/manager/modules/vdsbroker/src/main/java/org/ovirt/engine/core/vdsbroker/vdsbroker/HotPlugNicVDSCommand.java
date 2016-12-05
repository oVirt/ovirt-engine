package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VmNicDeviceVDSParameters;

public class HotPlugNicVDSCommand<P extends VmNicDeviceVDSParameters> extends HotPlugOrUnplugNicVDSCommand<P> {

    public HotPlugNicVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().hotPlugNic(createParametersStruct());
        proceedProxyReturnValue();
    }

}
