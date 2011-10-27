package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.asynctasks.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class DestroyImageVDSCommand<P extends DestroyImageVDSCommandParameters> extends IrsCreateCommand<P> {

    public DestroyImageVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        // LINQ 29456
        // uuidReturn =
        // IrsProxy.deleteVolume(DestroyParameters.StorageDomainId.toString(),
        // DestroyParameters.StoragePoolId.ToString(),
        // DestroyParameters.ImageGroupId.toString(),
        // DestroyParameters.ImageList.Select(a=>a.ToString()).ToArray(),
        // DestroyParameters.PostZero.ToString().ToLower());

        DestroyImageVDSCommandParameters params = getParameters();
        int imageListSize = params.getImageList().size();
        String[] volUUID = new String[imageListSize];
        int i = 0;
        for (Guid tempGuid : params.getImageList()) {
            volUUID[i++] = tempGuid.toString();
        }

        uuidReturn = getIrsProxy().deleteVolume(params.getStorageDomainId().toString(),
                                                params.getStoragePoolId().toString(),
                                                params.getImageGroupId().toString(),
                                                volUUID,
                                                String.valueOf(params.getPostZero()),
                                                String.valueOf(params.getForce()));

        // LINQ 29456
        ProceedProxyReturnValue();

        Guid taskID = new Guid(uuidReturn.mUuid);

        getVDSReturnValue()
                .setCreationInfo(
                        new AsyncTaskCreationInfo(taskID, AsyncTaskType.deleteVolume, getParameters()
                                .getStoragePoolId()));
    }
}
