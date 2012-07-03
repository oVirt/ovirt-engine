package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.vdscommands.UpdateSpmHostNameVDSCommandParameters;

public class UpdateSpmHostNameVDSCommand<P extends UpdateSpmHostNameVDSCommandParameters> extends IrsBrokerCommand<P> {
    public UpdateSpmHostNameVDSCommand(P parameters) {
        super(parameters);
    }

    // overriding ExecuteVDSCommand in order not to wait in getIrsProxy locking
    @Override
    protected void ExecuteVDSCommand() {
        // only if hostName in IrsProxy cache is the same as sent hostName
        // update to new hostName
        if (StringUtils.equals(getCurrentIrsProxyData().getmCurrentIrsHost(), getParameters().getOldHostName())) {
            getCurrentIrsProxyData().setmCurrentIrsHost(getParameters().getNewHostName());
        }
    }
}
