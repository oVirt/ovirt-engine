package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;

public class ResetISOPathVDSCommand<P extends IrsBaseVDSCommandParameters> extends IrsBrokerCommand<P> {
    public ResetISOPathVDSCommand(P parameters) {
        super(parameters);
    }

    // overriding executeVdsCommand and not IrsBroker command because no need in
    // spm for this action
    @Override
    protected void executeVDSCommand() {
        getCurrentIrsProxyData().setIsoPrefix("");
    }
}
