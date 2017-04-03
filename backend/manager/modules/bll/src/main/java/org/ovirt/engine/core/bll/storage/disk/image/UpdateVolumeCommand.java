package org.ovirt.engine.core.bll.storage.disk.image;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.EntityPollingCommand;
import org.ovirt.engine.core.bll.storage.StorageJobCommand;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.FenceVolumeJobCommandParameters;
import org.ovirt.engine.core.common.action.UpdateVolumeCommandParameters;
import org.ovirt.engine.core.common.businessentities.HostJobInfo.HostJobStatus;
import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.vdscommands.UpdateVolumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class UpdateVolumeCommand<T extends UpdateVolumeCommandParameters> extends StorageJobCommand<T> implements EntityPollingCommand {

    @Inject
    private VdsmImagePoller poller;

    public UpdateVolumeCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        completeGenerationInfo();

        UpdateVolumeVDSCommandParameters parameters =
                new UpdateVolumeVDSCommandParameters(getParameters().getStorageJobId(), getParameters().getVolInfo());
        parameters.setLegal(getParameters().getLegal());
        parameters.setDescription(getParameters().getDescription());
        parameters.setGeneration(getParameters().getGeneration());
        parameters.setShared(getParameters().getShared());

        vdsCommandsHelper.runVdsCommandWithoutFailover(VDSCommandType.UpdateVolume,
                parameters,
                getParameters().getStoragePoolId(),
                this);

        setSucceeded(true);
    }

    private void completeGenerationInfo() {
        VdsmImageLocationInfo info = getParameters().getVolInfo();
        DiskImage image = imagesHandler.getVolumeInfoFromVdsm(getParameters().getStoragePoolId(),
                info.getStorageDomainId(),
                info.getImageGroupId(),
                info.getImageId());
        info.setGeneration(image.getImage().getGeneration());
        persistCommandIfNeeded();
    }

    @Override
    public HostJobStatus poll() {
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
        FenceVolumeJobCommandParameters p = new FenceVolumeJobCommandParameters(info);
        p.setParentCommand(getActionType());
        p.setParentParameters(getParameters());
        p.setStoragePoolId(getParameters().getStoragePoolId());
        p.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);
        runInternalActionWithTasksContext(ActionType.FenceVolumeJob, p);
    }

}
