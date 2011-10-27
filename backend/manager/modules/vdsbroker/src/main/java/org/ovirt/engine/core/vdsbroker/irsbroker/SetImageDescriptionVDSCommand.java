package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.*;

public class SetImageDescriptionVDSCommand<P extends SetImageDescriptionVDSCommandParameters>
        extends IrsBrokerCommand<P> {
    public SetImageDescriptionVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        status = getIrsProxy().setVolumeDescription(getParameters().getStorageDomainId().toString(),
                getParameters().getStoragePoolId().toString(), getParameters().getImageGroupId().toString(),
                getParameters().getImageId().toString(), getParameters().getDescription());
        ProceedProxyReturnValue();
    }
}
