package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.*;

public class SetImageLegalityVDSCommand<P extends SetImageLegalityVDSCommandParameters> extends IrsBrokerCommand<P> {
    private String _legality = "";

    public SetImageLegalityVDSCommand(P parameters) {
        super(parameters);
        _legality = parameters.getIsLegal() ? "LEGAL" : "ILLEGAL";
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        status = getIrsProxy().setVolumeLegality(getParameters().getStorageDomainId().toString(),
                getParameters().getStoragePoolId().toString(), getParameters().getImageGroupId().toString(),
                getParameters().getImageId().toString(), _legality);
        ProceedProxyReturnValue();
    }
}
