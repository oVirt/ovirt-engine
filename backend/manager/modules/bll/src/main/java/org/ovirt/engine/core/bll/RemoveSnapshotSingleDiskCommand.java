package org.ovirt.engine.core.bll;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.MergeSnapshotsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.springframework.util.CollectionUtils;

@InternalCommandAttribute
public class RemoveSnapshotSingleDiskCommand<T extends ImagesContainterParametersBase> extends BaseImagesCommand<T> {
    public RemoveSnapshotSingleDiskCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        Guid storagePoolId = (getDiskImage().getStoragePoolId() != null) ?
                getDiskImage().getStoragePoolId() : Guid.Empty;

        Guid storageDomainId = !CollectionUtils.isEmpty(getDiskImage().getStorageIds()) ?
                getDiskImage().getStorageIds().get(0) : Guid.Empty;

        Guid taskId = persistAsyncTaskPlaceHolder(getParameters().getParentCommand());
        VDSReturnValue vdsReturnValue = mergeSnapshots(storagePoolId, storageDomainId);
        if (vdsReturnValue != null && vdsReturnValue.getCreationInfo() != null) {
            getReturnValue().getInternalVdsmTaskIdList().add(createTask(taskId, vdsReturnValue, storageDomainId));
            setSucceeded(vdsReturnValue.getSucceeded());
        } else {
            setSucceeded(false);
        }
    }

    private VDSReturnValue mergeSnapshots(Guid storagePoolId, Guid storageDomainId) {
        MergeSnapshotsVDSCommandParameters params = new MergeSnapshotsVDSCommandParameters(storagePoolId,
                storageDomainId, getVmId(), getDiskImage().getId(), getDiskImage().getImageId(),
                getDestinationDiskImage().getImageId(), getDiskImage().isWipeAfterDelete());

        return runVdsCommand(VDSCommandType.MergeSnapshots, params);
    }

    private Guid createTask(Guid taskId, VDSReturnValue vdsReturnValue, Guid storageDomainId) {
        String message = ExecutionMessageDirector.resolveStepMessage(StepEnum.MERGE_SNAPSHOTS,
                getJobMessageProperties());

        return super.createTask(taskId, vdsReturnValue.getCreationInfo(), getParameters().getParentCommand(),
                message, VdcObjectType.Storage, storageDomainId);
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.Disk.name().toLowerCase(), getDiskImage().getDiskAlias());
            jobProperties.put("sourcesnapshot",
                    getSnapshotDescriptionById(getDiskImage().getVmSnapshotId()));
            jobProperties.put("destinationsnapshot",
                    getSnapshotDescriptionById(getDestinationDiskImage().getVmSnapshotId()));
        }
        return jobProperties;
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.mergeSnapshots;
    }

    @Override
    protected void endSuccessfully() {
        // NOTE: The removal of the images from DB is done here
        // assuming that there might be situation (related to
        // tasks failures) in which we will want to preserve the
        // original state (before the merge-attempt).
        if (getDestinationDiskImage() != null) {
            DiskImage curr = getDestinationDiskImage();
            while (!curr.getParentId().equals(getDiskImage().getParentId())) {
                curr = getDiskImageDao().getSnapshotById(curr.getParentId());
                getImageDao().remove(curr.getImageId());
            }
            getDestinationDiskImage().setvolumeFormat(curr.getVolumeFormat());
            getDestinationDiskImage().setVolumeType(curr.getVolumeType());
            getDestinationDiskImage().setParentId(getDiskImage().getParentId());
            getBaseDiskDao().update(curr);
            getImageDao().update(getDestinationDiskImage().getImage());
            updateDiskImageDynamic();
        }

        setSucceeded(true);
    }

    private void updateDiskImageDynamic() {
        VDSReturnValue ret = runVdsCommand(
                VDSCommandType.GetImageInfo,
                new GetImageInfoVDSCommandParameters(getDestinationDiskImage().getStoragePoolId(),
                        getDestinationDiskImage().getStorageIds().get(0),
                        getDestinationDiskImage().getId(),
                        getDestinationDiskImage().getImageId()));

        // Update image's actual size in DB
        DiskImage imageFromIRS = (DiskImage) ret.getReturnValue();
        if (imageFromIRS != null) {
            completeImageData(imageFromIRS);
        } else {
            log.warnFormat("Could not update DiskImage's size with ID {0}",
                    getDestinationDiskImage().getImageId());
        }
    }

    @Override
    protected void endWithFailure() {
        // TODO: FILL! We should determine what to do in case of
        // failure (is everything rolled-backed? rolled-forward?
        // some and some?).
        setSucceeded(true);
    }

    private String getSnapshotDescriptionById(Guid snapshotId) {
        Snapshot snapshot = getSnapshotDao().get(snapshotId);
        return snapshot != null ? snapshot.getDescription() : StringUtils.EMPTY;
    }
}
