package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.EntityPollingCommand;
import org.ovirt.engine.core.bll.storage.StorageJobCommand;
import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CopyDataCommandParameters;
import org.ovirt.engine.core.common.action.FenceVolumeJobCommandParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.HostJobInfo.HostJobStatus;
import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.job.StepSubjectEntity;
import org.ovirt.engine.core.common.vdscommands.CopyVolumeDataVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class CopyDataCommand<T extends CopyDataCommandParameters> extends
        StorageJobCommand<T> implements EntityPollingCommand {

    @Inject
    private VdsmImagePoller poller;

    public CopyDataCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        completeGenerationInfo();
        VdsCommandsHelper.runVdsCommandWithFailover(VDSCommandType.CopyVolumeData,
                new CopyVolumeDataVDSCommandParameters(getParameters().getStorageJobId(),
                        getParameters().getSrcInfo(),
                        getParameters().getDstInfo(),
                        getParameters().isCollapse()),
                getParameters().getStoragePoolId(), this);
        setSucceeded(true);
    }

    private void completeGenerationInfo() {
        if (!isDstVdsmImage()) {
            return;
        }
        VdsmImageLocationInfo info = (VdsmImageLocationInfo) getParameters().getDstInfo();
        DiskImage image = ImagesHandler.getVolumeInfoFromVdsm(getParameters().getStoragePoolId(),
                info.getStorageDomainId(), info.getImageGroupId(), info.getImageId());
        info.setGeneration(image.getImage().getGeneration());
        persistCommandIfNeeded();
    }

    @Override
    public StepEnum getCommandStep() {
        return StepEnum.COPY_VOLUME;
    }

    @Override
    public boolean shouldUpdateStepProgress() {
        return true;
    }

    @Override
    public List<StepSubjectEntity> getCommandStepSubjectEntities() {
        if (getParameters().getJobWeight() != null && getParameters().getDstInfo() instanceof VdsmImageLocationInfo) {
            return Collections.singletonList(new StepSubjectEntity(VdcObjectType.Disk,
                    ((VdsmImageLocationInfo) getParameters().getDstInfo()).getImageGroupId(),
                    getParameters().getJobWeight()));
        }

        return super.getCommandStepSubjectEntities();
    }

    @Override
    public void attemptToFenceJob() {
        if (isDstVdsmImage()) {
            log.info("Command {} id: '{}': attempting to fence job {}",
                    getActionType(),
                    getCommandId(),
                    getJobId());
            VdsmImageLocationInfo info = (VdsmImageLocationInfo) getParameters().getDstInfo();
            FenceVolumeJobCommandParameters p = new FenceVolumeJobCommandParameters(info);
            p.setParentCommand(getActionType());
            p.setParentParameters(getParameters());
            p.setStoragePoolId(getParameters().getStoragePoolId());
            p.setEndProcedure(EndProcedure.COMMAND_MANAGED);
            runInternalActionWithTasksContext(VdcActionType.FenceVolumeJob, p);
        }
    }

    private boolean isDstVdsmImage() {
        return getParameters().getDstInfo() instanceof VdsmImageLocationInfo;
    }

    @Override
    public HostJobStatus poll() {
        if (isDstVdsmImage()) {
            VdsmImageLocationInfo info = (VdsmImageLocationInfo) getParameters().getDstInfo();
            return poller.pollImage(getParameters().getStoragePoolId(), info.getStorageDomainId(),
                    info.getImageGroupId(), info.getImageId(), info.getGeneration(), getCommandId(), getActionType());
        }

        return null;
    }
}
