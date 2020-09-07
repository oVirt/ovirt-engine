package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.EntityPollingCommand;
import org.ovirt.engine.core.bll.storage.StorageJobCommand;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ColdMergeCommandParameters;
import org.ovirt.engine.core.common.action.FenceVolumeJobCommandParameters;
import org.ovirt.engine.core.common.businessentities.HostJobInfo;
import org.ovirt.engine.core.common.businessentities.SubchainInfo;
import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.vdscommands.ColdMergeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class ColdMergeCommand<T extends ColdMergeCommandParameters> extends StorageJobCommand<T> implements EntityPollingCommand {

    @Inject
    private VdsmImagePoller poller;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private SnapshotDao snapshotDao;

    public ColdMergeCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        completeGenerationInfo();
        vdsCommandsHelper.runVdsCommandWithFailover(VDSCommandType.ColdMerge,
                new ColdMergeVDSCommandParameters(getParameters().getStorageJobId(),
                        getParameters().getSubchainInfo(), getParameters().isMergeBitmaps()),
                getParameters().getStoragePoolId(), this);
        setSucceeded(true);
    }

    @Override
    public HostJobInfo.HostJobStatus poll() {
        SubchainInfo info = getParameters().getSubchainInfo();
        return poller.pollImage(getParameters().getStoragePoolId(), info.getStorageDomainId(), info.getImageGroupId(),
                info.getBaseImageId(), info.getBaseImageGeneration(), getCommandId(), getActionType());
    }

    @Override
    public void attemptToFenceJob() {
        SubchainInfo info = getParameters().getSubchainInfo();
        VdsmImageLocationInfo locationInfo = new VdsmImageLocationInfo(info.getStorageDomainId(), info.getImageGroupId(),
                info.getBaseImageId(), info.getBaseImageGeneration());
        FenceVolumeJobCommandParameters parameters = new FenceVolumeJobCommandParameters(locationInfo);
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setStoragePoolId(getParameters().getStoragePoolId());
        parameters.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);
        runInternalActionWithTasksContext(ActionType.FenceVolumeJob, parameters);
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            DiskImage diskImage = diskImageDao.getSnapshotById(getParameters().getSubchainInfo().getTopImageId());
            DiskImage destDiskImage = diskImageDao.getSnapshotById(getParameters().getSubchainInfo().getBaseImageId());
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.Disk.name().toLowerCase(), diskImage.getDiskAlias());
            jobProperties.put("sourcesnapshot",
                    Optional.ofNullable(snapshotDao.get(diskImage.getVmSnapshotId()).getDescription()).orElse(""));
            jobProperties.put("destinationsnapshot",
                    Optional.ofNullable(snapshotDao.get(destDiskImage.getVmSnapshotId()).getDescription()).orElse(""));
        }
        return jobProperties;
    }

    @Override
    public StepEnum getCommandStep() {
        return StepEnum.MERGE_SNAPSHOTS;
    }

    private void completeGenerationInfo() {
        SubchainInfo info = getParameters().getSubchainInfo();
        DiskImage image = imagesHandler.getVolumeInfoFromVdsm(getParameters().getStoragePoolId(),
                info.getStorageDomainId(), info.getImageGroupId(), info.getBaseImageId());
        info.setBaseImageGeneration(image.getImage().getGeneration());
        persistCommandIfNeeded();
    }
}
