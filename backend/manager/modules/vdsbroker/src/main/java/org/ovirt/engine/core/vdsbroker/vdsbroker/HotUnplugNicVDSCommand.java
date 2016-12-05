package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VmNicDeviceVDSParameters;

public class HotUnplugNicVDSCommand<P extends VmNicDeviceVDSParameters> extends HotPlugOrUnplugNicVDSCommand<P> {

    public HotUnplugNicVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().hotUnplugNic(createParametersStruct());
        proceedProxyReturnValue();
    }

}
