package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.SyncImageGroupDataVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;

public class SyncImageGroupDataVDSCommand<P extends SyncImageGroupDataVDSCommandParameters> extends IrsCreateCommand<P> {
    public SyncImageGroupDataVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        P params = getParameters();

        uuidReturn = getIrsProxy().syncImageData(params.getStoragePoolId().toString(),
                params.getStorageDomainId().toString(),
                params.getImageGroupId().toString(),
                params.getDstDomainId().toString(),
                params.getSyncType());

        proceedProxyReturnValue();

        Guid taskID = new Guid(uuidReturn.uuid);

        getVDSReturnValue().setCreationInfo(
                new AsyncTaskCreationInfo(taskID, AsyncTaskType.syncImageData, params.getStoragePoolId()));
    }
}
