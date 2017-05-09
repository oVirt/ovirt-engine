package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VmNicDeviceVDSParameters;

public class HotPlugNicVDSCommand<P extends VmNicDeviceVDSParameters> extends HotPlugOrUnplugNicVDSCommand<P> {

    private VmInfoReturn result;

    public HotPlugNicVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        result = getBroker().hotPlugNic(createParametersStruct());
        proceedProxyReturnValue();
        setReturnValue(result);
     }

     @Override
     protected Status getReturnStatus() {
         return result.getStatus();
     }

     @Override
     protected Object getReturnValueFromBroker() {
         return result;
     }
}
