package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.HSMGetIsoListParameters;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

public class GetIsoListVDSCommand<P extends IrsBaseVDSCommandParameters> extends IrsBrokerCommand<P> {
    public GetIsoListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        setReturnValue(resourceManager
                .runVdsCommand(
                        VDSCommandType.HsmGetIsoList,
                        new HSMGetIsoListParameters(getCurrentIrsProxyData().getCurrentVdsId(),
                                getParameters().getStoragePoolId()))
                .getReturnValue());
    }
}
