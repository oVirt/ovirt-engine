package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.*;

/**
 * The following command should remove a vg , the command is not supported from vdsm 3.0
 * only suitable for vdsm 2.2.
 */
@Deprecated
public class RemoveVGVDSCommand<P extends RemoveVGVDSCommandParameters> extends VdsBrokerCommand<P> {
    public RemoveVGVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().removeVG(getParameters().getVGID());
        ProceedProxyReturnValue();
    }
}
