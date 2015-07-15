package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.TargetDomainImageGroupVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.storage.StorageDomainHelper;

public class CloneImageGroupStructureVDSCommand<P extends TargetDomainImageGroupVDSCommandParameters> extends IrsCreateCommand<P> {
    public CloneImageGroupStructureVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        StorageDomainHelper.checkNumberOfLVsForBlockDomain(getParameters().getDstDomainId());
        P params = getParameters();
        uuidReturn = getIrsProxy().cloneImageStructure(params.getStoragePoolId().toString(),
                        params.getStorageDomainId().toString(),
                        params.getImageGroupId().toString(),
                        params.getDstDomainId().toString());

        proceedProxyReturnValue();

        Guid taskID = new Guid(uuidReturn.uuid);

        getVDSReturnValue().setCreationInfo
                (new AsyncTaskCreationInfo(taskID, AsyncTaskType.cloneImageStructure, params.getStoragePoolId()));
    }

}
