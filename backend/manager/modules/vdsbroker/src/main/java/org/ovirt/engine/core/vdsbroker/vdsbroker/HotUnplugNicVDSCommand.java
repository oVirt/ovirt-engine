package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VmNicDeviceVDSParameters;

public class HotUnplugNicVDSCommand<P extends VmNicDeviceVDSParameters> extends HotPlugNicVDSCommand<P> {

    public HotUnplugNicVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVDSCommand() {
        //TODO MMUCHA: Dear code reviewer! This seems *very* wild. It overrides all error handling of 'executeVDSCommand' — I believe author wanted to override 'executeVdsBrokerCommand' just to change broker method. If that's true (please confirm), then it's weird that we need same data for unplug, and if we do, we should make common superclass, since this would be OO design failure. Please advise.
        init();
        status = getBroker().hotUnplugNic(struct);
        proceedProxyReturnValue();
    }

}
