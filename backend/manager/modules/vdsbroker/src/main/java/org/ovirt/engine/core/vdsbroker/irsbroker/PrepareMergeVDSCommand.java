package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.utils.SubchainInfoHelper;
import org.ovirt.engine.core.common.vdscommands.SPMColdMergeVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class PrepareMergeVDSCommand<T extends SPMColdMergeVDSCommandParameters> extends IrsBrokerCommand<T> {

    private OneUuidReturn uuidReturn;

    public PrepareMergeVDSCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        uuidReturn = getIrsProxy().prepareMerge(getParameters().getStoragePoolId().toString(),
                SubchainInfoHelper.prepareSubchainInfoForVdsCommand(getParameters().getSubchainInfo()));

        proceedProxyReturnValue();

        Guid taskID = new Guid(uuidReturn.uuid);

        getVDSReturnValue().setCreationInfo(new AsyncTaskCreationInfo(taskID, AsyncTaskType.prepareMerge,
                getParameters().getStoragePoolId()));
    }

    @Override
    protected Status getReturnStatus() {
        return uuidReturn.getStatus();
    }
}
