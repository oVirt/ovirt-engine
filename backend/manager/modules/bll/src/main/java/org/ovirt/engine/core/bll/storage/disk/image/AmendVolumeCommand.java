package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.EntityPollingCommand;
import org.ovirt.engine.core.bll.storage.StorageJobCommand;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AmendVolumeCommandParameters;
import org.ovirt.engine.core.common.action.FenceVolumeJobCommandParameters;
import org.ovirt.engine.core.common.businessentities.HostJobInfo;
import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.Image;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.job.StepSubjectEntity;
import org.ovirt.engine.core.common.vdscommands.AmendVolumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ImageDao;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class AmendVolumeCommand<T extends AmendVolumeCommandParameters> extends
        StorageJobCommand<T> implements EntityPollingCommand {

    @Inject
    private VdsmImagePoller poller;

    @Inject
    private ImageDao imageDao;

    private Image image;

    public AmendVolumeCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    public AmendVolumeCommand(Guid commandId) {
        super(commandId);
    }

    private Image getImage() {
        if (image == null) {
            VdsmImageLocationInfo info = (VdsmImageLocationInfo) getParameters().getVolInfo();
            image = imageDao.get(info.getImageId());
        }
        return image;
    }

    @Override
    protected void executeCommand() {
        VdsmImageLocationInfo info = (VdsmImageLocationInfo) getParameters().getVolInfo();
        DiskImage image = imagesHandler.getVolumeInfoFromVdsm(getParameters().getStoragePoolId(),
                info.getStorageDomainId(), info.getImageGroupId(), info.getImageId());
        info.setGeneration(image.getImage().getGeneration());
        persistCommandIfNeeded();

        VDSReturnValue vdsReturnValue = vdsCommandsHelper.runVdsCommandWithFailover(VDSCommandType.AmendVolume,
                new AmendVolumeVDSCommandParameters(getParameters().getStorageJobId(),
                        info.getStorageDomainId(),
                        info.getImageGroupId(),
                        info.getImageId(),
                        info.getGeneration(),
                        getParameters().getQcowCompat()),
                getParameters().getStoragePoolId(), this);
        if (!vdsReturnValue.getSucceeded()) {
            setCommandStatus(CommandStatus.FAILED);
        }
        setSucceeded(vdsReturnValue.getSucceeded());
    }

    @Override
    public StepEnum getCommandStep() {
        return StepEnum.AMEND_VOLUME;
    }

    @Override
    public boolean shouldUpdateStepProgress() {
        return true;
    }

    @Override
    public List<StepSubjectEntity> getCommandStepSubjectEntities() {
        if (getParameters().getJobWeight() != null && getParameters().getVolInfo() instanceof VdsmImageLocationInfo) {
            return Collections.singletonList(new StepSubjectEntity(VdcObjectType.Disk,
                    ((VdsmImageLocationInfo) getParameters().getVolInfo()).getImageGroupId(),
                    getParameters().getJobWeight()));
        }

        return super.getCommandStepSubjectEntities();
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
        }
        jobProperties.put(StorageConstants.GUID, getImage().getId().toString());
        return jobProperties;
    }

    private boolean isVdsmImage() {
        return getParameters().getVolInfo() instanceof VdsmImageLocationInfo;
    }

    @Override
    public HostJobInfo.HostJobStatus poll() {
        if (isVdsmImage()) {
            VdsmImageLocationInfo info = (VdsmImageLocationInfo) getParameters().getVolInfo();
            return poller.pollImage(getParameters().getStoragePoolId(), info.getStorageDomainId(),
                    info.getImageGroupId(), info.getImageId(), info.getGeneration(), getCommandId(), getActionType());
        }
        return null;
    }

    @Override
    public void attemptToFenceJob() {
        VdsmImageLocationInfo info = (VdsmImageLocationInfo) getParameters().getVolInfo();
        FenceVolumeJobCommandParameters parameters = new FenceVolumeJobCommandParameters(info);
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setStoragePoolId(getParameters().getStoragePoolId());
        parameters.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);
        runInternalActionWithTasksContext(ActionType.FenceVolumeJob, parameters);
    }

    protected void endSuccessfully() {
        getImage().setQcowCompat(getParameters().getQcowCompat());
        imageDao.update(getImage());
        setSucceeded(true);
    }

}
