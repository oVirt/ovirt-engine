package org.ovirt.engine.core.vdsbroker.irsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.MergeSnapshotsVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;

public class MergeSnapshotsVDSCommand<P extends MergeSnapshotsVDSCommandParameters> extends IrsCreateCommand<P> {

    @Inject
    private StoragePoolDao storagePoolDao;

    public MergeSnapshotsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        uuidReturn = getIrsProxy().mergeSnapshots(getParameters().getStorageDomainId().toString(),
                                                  getParameters().getStoragePoolId().toString(),
                                                  getParameters().getVmId().toString(),
                                                  getParameters().getImageGroupId().toString(),
                                                  getParameters().getImageId().toString(),
                                                  getParameters().getImageId2().toString(),
                                                  String.valueOf(getParameters().getPostZero()).toLowerCase(),
                                                  getParameters().isDiscard());

        proceedProxyReturnValue();

        Guid taskID = new Guid(uuidReturn.uuid);

        getVDSReturnValue()
                .setCreationInfo(
                        new AsyncTaskCreationInfo(taskID, AsyncTaskType.mergeSnapshots, getParameters()
                                .getStoragePoolId()));
    }
}
