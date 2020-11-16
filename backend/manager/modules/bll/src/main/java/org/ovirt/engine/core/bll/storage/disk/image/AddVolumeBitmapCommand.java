package org.ovirt.engine.core.bll.storage.disk.image;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.EntityPollingCommand;
import org.ovirt.engine.core.bll.storage.StorageJobCommand;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVolumeBitmapCommandParameters;
import org.ovirt.engine.core.common.action.FenceVolumeJobCommandParameters;
import org.ovirt.engine.core.common.businessentities.HostJobInfo;
import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.vdscommands.ColdVmBackupVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;

@NonTransactiveCommandAttribute(forceCompensation = true)
@InternalCommandAttribute
public class AddVolumeBitmapCommand<T extends AddVolumeBitmapCommandParameters> extends
        StorageJobCommand<T> implements EntityPollingCommand {

    @Inject
    private VdsmImagePoller poller;

    public AddVolumeBitmapCommand(Guid commandId) {
        super(commandId);
    }

    public AddVolumeBitmapCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void executeCommand() {
        VdsmImageLocationInfo info = getParameters().getVolInfo();
        DiskImage image = imagesHandler.getVolumeInfoFromVdsm(
                getParameters().getStoragePoolId(),
                info.getStorageDomainId(),
                info.getImageGroupId(),
                info.getImageId());
        info.setGeneration(image.getImage().getGeneration());
        persistCommandIfNeeded();

        VDSReturnValue vdsReturnValue;
        try {
            vdsReturnValue = vdsCommandsHelper.runVdsCommandWithFailover(
                    VDSCommandType.AddVolumeBitmap,
                    new ColdVmBackupVDSCommandParameters(
                            info.getStorageDomainId(),
                            getParameters().getVdsId(),
                            getParameters().getStorageJobId(),
                            info.getImageGroupId(),
                            info.getImageId(),
                            info.getGeneration(),
                            getParameters().getBitmapName()),
                    getParameters().getStoragePoolId(),
                    this);
        } catch (EngineException e) {
            log.error("Failed to add bitmap to image id '{}'", info.getImageId());
            throw e;
        }

        if (!vdsReturnValue.getSucceeded()) {
            setCommandStatus(CommandStatus.FAILED);
        }
        setSucceeded(vdsReturnValue.getSucceeded());
    }

    @Override
    public HostJobInfo.HostJobStatus poll() {
        VdsmImageLocationInfo info = getParameters().getVolInfo();
        return poller.pollImage(getParameters().getStoragePoolId(),
                info.getStorageDomainId(),
                info.getImageGroupId(),
                info.getImageId(),
                info.getGeneration(),
                getCommandId(),
                getActionType());
    }

    @Override
    public void attemptToFenceJob() {
        log.info("Command {} id: '{}': attempting to fence job {}",
                getActionType(),
                getCommandId(),
                getJobId());
        VdsmImageLocationInfo info = getParameters().getVolInfo();
        FenceVolumeJobCommandParameters parameters = new FenceVolumeJobCommandParameters(info);
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setStoragePoolId(getParameters().getStoragePoolId());
        parameters.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);
        runInternalActionWithTasksContext(ActionType.FenceVolumeJob, parameters);
    }
}
