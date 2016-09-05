package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;

public class IsoDirectoryVDSCommand<P extends IrsBaseVDSCommandParameters> extends IrsBrokerCommand<P> {
    public IsoDirectoryVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        getVDSReturnValue().setReturnValue(getCurrentIrsProxy().getIsoDirectory());
    }
}
