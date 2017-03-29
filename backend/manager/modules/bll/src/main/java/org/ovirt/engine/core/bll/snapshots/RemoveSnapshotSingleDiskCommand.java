package org.ovirt.engine.core.bll.snapshots;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.domain.PostDeleteActionHandler;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VmBlockJobType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.vdscommands.MergeSnapshotsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.utils.transaction.NoOpTransactionCompletionListener;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@InternalCommandAttribute
public class RemoveSnapshotSingleDiskCommand<T extends ImagesContainterParametersBase> extends RemoveSnapshotSingleDiskCommandBase<T> {

    @Inject
    private PostDeleteActionHandler postDeleteActionHandler;

    public RemoveSnapshotSingleDiskCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        registerRollbackHandler(new CustomTransactionCompletionListener());
        Guid storagePoolId = (getDiskImage().getStoragePoolId() != null) ?
                getDiskImage().getStoragePoolId() : Guid.Empty;

        Guid storageDomainId = !CollectionUtils.isEmpty(getDiskImage().getStorageIds()) ?
                getDiskImage().getStorageIds().get(0) : Guid.Empty;

        Guid taskId = persistAsyncTaskPlaceHolder(getParameters().getParentCommand());
        VDSReturnValue vdsReturnValue = mergeSnapshots(storagePoolId, storageDomainId);
        if (vdsReturnValue != null && vdsReturnValue.getCreationInfo() != null) {
            getTaskIdList().add(createTask(taskId, vdsReturnValue, storageDomainId));
            setSucceeded(vdsReturnValue.getSucceeded());
        } else {
            setSucceeded(false);
        }
    }

    protected VDSReturnValue mergeSnapshots(Guid storagePoolId, Guid storageDomainId) {
        MergeSnapshotsVDSCommandParameters params = new MergeSnapshotsVDSCommandParameters(storagePoolId,
                storageDomainId, getVmId(), getDiskImage().getId(), getDiskImage().getImageId(),
                getDestinationDiskImage().getImageId(), getDiskImage().isWipeAfterDelete(),
                storageDomainDao.get(storageDomainId).isDiscardAfterDelete());
        return runVdsCommand(VDSCommandType.MergeSnapshots,
                postDeleteActionHandler.fixParameters(params));
    }

    protected Guid createTask(Guid taskId, VDSReturnValue vdsReturnValue, Guid storageDomainId) {
        String message = ExecutionMessageDirector.resolveStepMessage(StepEnum.MERGE_SNAPSHOTS,
                getJobMessageProperties());
        return super.createTask(taskId, vdsReturnValue.getCreationInfo(), getParameters().getParentCommand(),
                message, VdcObjectType.Storage, storageDomainId);
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
            Set<Guid> imagesToUpdate = new HashSet<>();
            DiskImage curr = getDestinationDiskImage();
            while (!curr.getParentId().equals(getDiskImage().getParentId())) {
                curr = diskImageDao.getSnapshotById(curr.getParentId());
                imagesToUpdate.add(curr.getImageId());
            }
            syncDbRecords(VmBlockJobType.PULL,
                    getImageInfoFromVdsm(getDestinationDiskImage()),
                    imagesToUpdate,
                    true);
        }

        if (getParameters().getVmSnapshotId() != null) {
            lockVmSnapshotsWithWait(getVm());
            Snapshot snapshot = snapshotDao.get(getParameters().getVmSnapshotId());
            Snapshot snapshotWithoutImage =
                    imagesHandler.prepareSnapshotConfigWithoutImageSingleImage(snapshot, getParameters().getImageId(),
                            ovfManager);
            snapshotDao.update(snapshotWithoutImage);
            if (getSnapshotsEngineLock() != null) {
                lockManager.releaseLock(getSnapshotsEngineLock());
            }
        }

        setSucceeded(true);
    }

    private class CustomTransactionCompletionListener extends NoOpTransactionCompletionListener {
        @Override
        public void onRollback() {
            TransactionSupport.executeInNewTransaction(() -> {
                if (!getParameters().isLeaveLocked()) {
                    DiskImage diskImage = getDestinationDiskImage();
                    if (diskImage != null) {
                        imageDao.updateStatus(diskImage.getImage().getId(), ImageStatus.OK);
                    }
                    unLockImage();
                }
                return null;
            });
        }
    }
}
