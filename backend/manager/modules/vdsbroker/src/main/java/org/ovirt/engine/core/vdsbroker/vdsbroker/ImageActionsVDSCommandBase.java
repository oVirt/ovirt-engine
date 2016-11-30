package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.ImageActionsVDSCommandParameters;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public abstract class ImageActionsVDSCommandBase<P extends ImageActionsVDSCommandParameters> extends VdsBrokerCommand<P> {
    private StatusReturn status;

    public ImageActionsVDSCommandBase(P parameters) {
        super(parameters);
    }

    @Override protected void executeVdsBrokerCommand() {
        status = executeImageActionVdsBrokerCommand(
                getParameters().getStoragePoolId().toString(),
                getParameters().getStorageDomainId().toString(),
                getParameters().getImageGroupId().toString(),
                getParameters().getImageId().toString());

        proceedProxyReturnValue();
        setReturnValue(status);
    }

    protected abstract StatusReturn executeImageActionVdsBrokerCommand(String spId,
            String sdId,
            String imgGroupId,
            String imgId);

    @Override
    protected Status getReturnStatus() {
        return status.getStatus();
    }
}
