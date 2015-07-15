package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.MergeSnapshotsVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;

public class MergeSnapshotsVDSCommand<P extends MergeSnapshotsVDSCommandParameters> extends IrsCreateCommand<P> {
    public MergeSnapshotsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        uuidReturn =
                getIrsProxy().mergeSnapshots(getParameters().getStorageDomainId().toString(),
                                                  getParameters().getStoragePoolId().toString(),
                                                  getParameters().getVmId().toString(),
                                                  getParameters().getImageGroupId().toString(),
                                                  getParameters().getImageId().toString(),
                                                  getParameters().getImageId2().toString(),
                                                  String.valueOf(getParameters().getPostZero()).toLowerCase());

        proceedProxyReturnValue();

        Guid taskID = new Guid(uuidReturn.uuid);

        getVDSReturnValue()
                .setCreationInfo(
                        new AsyncTaskCreationInfo(taskID, AsyncTaskType.mergeSnapshots, getParameters()
                                .getStoragePoolId()));
    }
}
