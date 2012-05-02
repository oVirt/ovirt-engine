package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.async_tasks;
import org.ovirt.engine.core.common.vdscommands.MergeSnapshotsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

@InternalCommandAttribute
public class RemoveSnapshotSingleDiskCommand<T extends ImagesContainterParametersBase> extends BaseImagesCommand<T> {
    public RemoveSnapshotSingleDiskCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        Guid storagePoolId = getDiskImage().getstorage_pool_id() != null ? getDiskImage().getstorage_pool_id()
                .getValue() : Guid.Empty;
        Guid storageDomainId =
                getDiskImage().getstorage_ids() != null && getDiskImage().getstorage_ids().size() > 0 ? getDiskImage().getstorage_ids()
                        .get(0)
                        : Guid.Empty;
        Guid imageGroupId = getDiskImage().getimage_group_id() != null ? getDiskImage().getimage_group_id().getValue()
                : Guid.Empty;

        VDSReturnValue vdsReturnValue = Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(
                        VDSCommandType.MergeSnapshots,
                        new MergeSnapshotsVDSCommandParameters(storagePoolId, storageDomainId, getVmId(), imageGroupId,
                                getDiskImage().getImageId(), getDestinationDiskImage().getImageId(),
                                getDiskImage().isWipeAfterDelete(), getStoragePool().getcompatibility_version()
                                        .toString()));

        if (vdsReturnValue != null && vdsReturnValue.getCreationInfo() != null) {
            getReturnValue().getInternalTaskIdList().add(
                    CreateTask(vdsReturnValue.getCreationInfo(), VdcActionType.RemoveSnapshot));
            setSucceeded(vdsReturnValue.getSucceeded());
        } else {
            setSucceeded(false);
        }
    }

    @Override
    protected Guid ConcreteCreateTask(AsyncTaskCreationInfo asyncTaskCreationInfo, VdcActionType parentCommand) {
        AsyncTaskParameters p = new AsyncTaskParameters(asyncTaskCreationInfo, new async_tasks(parentCommand,
                AsyncTaskResultEnum.success, AsyncTaskStatusEnum.running, asyncTaskCreationInfo.getTaskID(),
                getParameters(), asyncTaskCreationInfo.getStepId(), getCommandId()));
        p.setEntityId(getParameters().getEntityId());
        Guid ret = AsyncTaskManager.getInstance().CreateTask(AsyncTaskType.mergeSnapshots, p, false);

        return ret;
    }

    @Override
    protected void EndSuccessfully() {
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
            getDestinationDiskImage().setvolume_format(curr.getvolume_format());
            getDestinationDiskImage().setvolume_type(curr.getvolume_type());
            getDestinationDiskImage().setParentId(getDiskImage().getParentId());
            getBaseDiskDao().update(curr);
            getImageDao().update(getDestinationDiskImage().getImage());
        }

        setSucceeded(true);
    }

    @Override
    protected void EndWithFailure() {
        // TODO: FILL! We should determine what to do in case of
        // failure (is everything rolled-backed? rolled-forward?
        // some and some?).
        setSucceeded(true);
    }
}
