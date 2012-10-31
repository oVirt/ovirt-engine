package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.vdsbroker.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class GetIsoListVDSCommand<P extends IrsBaseVDSCommandParameters> extends IrsBrokerCommand<P> {
    public GetIsoListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        setReturnValue(ResourceManager
                .getInstance()
                .runVdsCommand(
                        VDSCommandType.HsmGetIsoList,
                        new HSMGetIsoListParameters(getCurrentIrsProxyData().getCurrentVdsId(),
                                getParameters().getStoragePoolId()))
                .getReturnValue());
    }
}
