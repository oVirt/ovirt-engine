package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.HotPlugUnplgNicVDSParameters;

public class HotUnplugNicVDSCommand<P extends HotPlugUnplgNicVDSParameters> extends HotPlugNicVDSCommand<P> {

    public HotUnplugNicVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVDSCommand() {
        init();
        status = getBroker().hotUnplugNic(struct);
        ProceedProxyReturnValue();
    }

}
