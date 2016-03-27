package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.SetVolumeLegalityVDSCommandParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;

public class SetVolumeLegalityVDSCommand <P extends SetVolumeLegalityVDSCommandParameters> extends IrsBrokerCommand<P> {
    private StatusReturnForXmlRpc result;
    public SetVolumeLegalityVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        String legality = getParameters().getLegality() ? "LEGAL" : "ILLEGAL";
        SetVolumeLegalityVDSCommandParameters parameters = getParameters();

        result = getIrsProxy().setVolumeLegality(
                parameters.getStoragePoolId().toString(),
                parameters.getStorageDomainId().toString(),
                parameters.getImageGroupId().toString(),
                parameters.getImageId().toString(),
                legality
        );

        proceedProxyReturnValue();
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return result.getXmlRpcStatus();
    }
}
