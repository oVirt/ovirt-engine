package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.ReduceImageVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReduceImageVDSCommand<P extends ReduceImageVDSCommandParameters> extends IrsBrokerCommand<P> {
    private static final Logger log = LoggerFactory.getLogger(ReduceImageVDSCommand.class);

    private OneUuidReturn uuidReturn;

    public ReduceImageVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        log.info("Executing ReduceImageVDSCommand");

        uuidReturn = getIrsProxy().reduceVolume(getParameters().getStoragePoolId().toString(),
                getParameters().getStorageDomainId().toString(),
                getParameters().getImageGroupId().toString(),
                getParameters().getImageId().toString(),
                getParameters().isAllowActive());

        proceedProxyReturnValue();

        Guid taskID = new Guid(uuidReturn.uuid);

        getVDSReturnValue().setCreationInfo(new AsyncTaskCreationInfo(taskID, AsyncTaskType.reduceImage,
                getParameters().getStoragePoolId()));
    }

    @Override
    protected Status getReturnStatus() {
        return uuidReturn.getStatus();
    }
}
