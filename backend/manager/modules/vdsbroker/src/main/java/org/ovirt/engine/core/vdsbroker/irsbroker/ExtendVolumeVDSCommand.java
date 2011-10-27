package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.*;

public class ExtendVolumeVDSCommand<P extends ExtendVolumeVDSCommandParameters> extends IrsBrokerCommand<P> {
    public ExtendVolumeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        status = getIrsProxy().extendVolume(getParameters().getStorageDomainId().toString(),
                getParameters().getStoragePoolId().toString(),
                getParameters().getImageGroupId().toString(), getParameters().getImageId().toString(),
                getParameters().getNewSize());
        ProceedProxyReturnValue();
    }
}
