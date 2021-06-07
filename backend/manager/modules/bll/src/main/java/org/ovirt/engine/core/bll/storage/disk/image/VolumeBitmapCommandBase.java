package org.ovirt.engine.core.bll.storage.disk.image;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.EntityPollingCommand;
import org.ovirt.engine.core.bll.storage.StorageJobCommand;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.FenceVolumeJobCommandParameters;
import org.ovirt.engine.core.common.action.VolumeBitmapCommandParameters;
import org.ovirt.engine.core.common.businessentities.HostJobInfo;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VolumeBitmapVDSCommandParameters;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;

public abstract class VolumeBitmapCommandBase<T extends VolumeBitmapCommandParameters> extends
        StorageJobCommand<T> implements EntityPollingCommand {

    @Inject
    private VdsmImagePoller poller;

    @Override
    protected void init() {
        super.init();
        Guid vdsId;
        if (!Guid.isNullOrEmpty(getParameters().getVdsId())) {
            vdsId = getParameters().getVdsId();
        } else {
            vdsId = getBitmapAction().equals(VDSCommandType.ClearVolumeBitmaps)
                    ? vdsCommandsHelper.getHostForExecution(getParameters().getStoragePoolId(),
                            VDS::isClearBitmapsEnabled)
                    : vdsCommandsHelper.getHostForExecution(getParameters().getStoragePoolId(),
                            VDS::isColdBackupEnabled);
        }
        setVdsId(vdsId);
        getParameters().setVdsId(getVdsId());
        getParameters().setVdsRunningOn(getVdsId());
    }

    public VolumeBitmapCommandBase(Guid commandId) {
        super(commandId);
    }

    public VolumeBitmapCommandBase(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    protected abstract VDSCommandType getBitmapAction();

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

        VolumeBitmapVDSCommandParameters parameters = new VolumeBitmapVDSCommandParameters(
                info.getStorageDomainId(),
                getParameters().getVdsId(),
                getParameters().getStorageJobId(),
                info.getImageGroupId(),
                info.getImageId(),
                info.getGeneration(),
                getBitmapAction() != VDSCommandType.ClearVolumeBitmaps ? getParameters().getBitmapName() : null);
        parameters.setVdsId(getVdsId());

        VDSReturnValue vdsReturnValue;
        try {
            vdsReturnValue = vdsCommandsHelper.runVdsCommandWithFailover(
                    getBitmapAction(), parameters, getParameters().getStoragePoolId(), this);
        } catch (EngineException e) {
            log.error("Failed to perform bitmap operation to image id '{}'", info.getImageId());
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
