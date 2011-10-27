package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.asynctasks.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class MergeSnapshotsVDSCommand<P extends MergeSnapshotsVDSCommandParameters> extends IrsCreateCommand<P> {
    public MergeSnapshotsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        uuidReturn =
                getIrsProxy().mergeSnapshots(getParameters().getStorageDomainId().toString(),
                                                  getParameters().getStoragePoolId().toString(),
                                                  getParameters().getVmId().toString(),
                                                  getParameters().getImageGroupId().toString(),
                                                  getParameters().getImageId().toString(),
                                                  getParameters().getImageId2().toString(),
                                                  (new Boolean(getParameters().getPostZero())).toString().toLowerCase());

        ProceedProxyReturnValue();

        Guid taskID = new Guid(uuidReturn.mUuid);

        getVDSReturnValue()
                .setCreationInfo(
                        new AsyncTaskCreationInfo(taskID, AsyncTaskType.mergeSnapshots, getParameters()
                                .getStoragePoolId()));
    }
}
