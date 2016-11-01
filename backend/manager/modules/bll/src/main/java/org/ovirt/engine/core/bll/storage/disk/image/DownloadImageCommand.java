package org.ovirt.engine.core.bll.storage.disk.image;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.DownloadImageCommandParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.DownloadImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class DownloadImageCommand<T extends DownloadImageCommandParameters> extends BaseImagesCommand<T> {

    public DownloadImageCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void executeCommand() {
        persistAsyncTaskPlaceHolder(getParameters().getParentCommand());
        VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.DownloadImage, getVDSParameters());
        if (vdsReturnValue != null && vdsReturnValue.getSucceeded()) {
            getTaskIdList().add(
                    createTask(getAsyncTaskId(),
                            vdsReturnValue.getCreationInfo(),
                            getParameters().getParentCommand(),
                            VdcObjectType.Disk,
                            getParameters().getDestinationImageId()));
        }

        setSucceeded(vdsReturnValue != null ? vdsReturnValue.getSucceeded() : false);
    }

    @Override
    public AsyncTaskType getTaskType() {
        return AsyncTaskType.downloadImage;
    }

    protected VDSParametersBase getVDSParameters() {
        return new DownloadImageVDSCommandParameters(
                getParameters().getStoragePoolId(),
                getParameters().getStorageDomainId(),
                getParameters().getImageGroupID(),
                getParameters().getDestinationImageId(),
                getParameters().getHttpLocationInfo());
    }
}
