package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.RemoveImageParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageOperation;
import org.ovirt.engine.core.common.businessentities.async_tasks;
import org.ovirt.engine.core.common.businessentities.image_storage_domain_map;
import org.ovirt.engine.core.common.businessentities.image_storage_domain_map_id;
import org.ovirt.engine.core.common.vdscommands.CopyImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.MoveImageGroupVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

@SuppressWarnings("serial")
@InternalCommandAttribute
public class MoveOrCopyImageGroupCommand<T extends MoveOrCopyImageGroupParameters> extends BaseImagesCommand<T> {
    public MoveOrCopyImageGroupCommand(T parameters) {
        super(parameters);
    }

    private DiskImage _diskImage;

    @Override
    protected DiskImage getImage() {
        switch (getActionState()) {
        case END_SUCCESS:
        case END_FAILURE:
            if (_diskImage == null) {
                List<DiskImage> diskImages =
                        getDiskImageDAO().getAllSnapshotsForImageGroup(getParameters().getImageGroupID());
                _diskImage = (diskImages.isEmpty()) ? null : diskImages.get(0);
            }

            return _diskImage;

        default:
            return super.getImage();
        }
    }

    protected ImageOperation getMoveOrCopyImageOperation() {
        return getParameters().getOperation();
    }

    @Override
    protected void executeCommand() {
        LockImage();
        VDSReturnValue vdsReturnValue = null;

        if (getParameters().getUseCopyCollapse()) {
            vdsReturnValue = runVdsCommand(
                    VDSCommandType.CopyImage,
                    new CopyImageVDSCommandParameters(getStorageDomain().getstorage_pool_id()
                            .getValue(),
                            getParameters().getSourceDomainId() != null ? getParameters().getSourceDomainId()
                                    .getValue()
                                    : getDiskImage().getstorage_ids().get(0),
                            getParameters()
                                    .getContainerId(),
                            getParameters().getImageGroupID(),
                            getParameters()
                                    .getImageId(),
                            getParameters().getDestImageGroupId(),
                            getParameters().getDestinationImageId(),
                            "",
                            getParameters().getStorageDomainId(),
                            getParameters()
                                    .getCopyVolumeType(),
                            getParameters().getVolumeFormat(),
                            getParameters()
                                    .getVolumeType(),
                            getParameters().getPostZero(),
                            getParameters()
                                    .getForceOverride(),
                            getStoragePool().getcompatibility_version().toString()));
        } else {
            vdsReturnValue = runVdsCommand(
                    VDSCommandType.MoveImageGroup,
                    new MoveImageGroupVDSCommandParameters(getDiskImage().getstorage_pool_id()
                            .getValue(),
                            getParameters().getSourceDomainId() != null ? getParameters().getSourceDomainId()
                                    .getValue()
                                    : getDiskImage().getstorage_ids().get(0),
                            getDiskImage()
                                    .getId(),
                            getParameters().getStorageDomainId(),
                            getParameters().getContainerId(),
                            getParameters().getOperation(),
                            getParameters()
                                    .getPostZero(),
                            getParameters().getForceOverride(),
                            getStoragePool()
                                    .getcompatibility_version().toString()));
        }

        if (vdsReturnValue.getSucceeded()) {
            AsyncTaskCreationInfo taskCreationInfo = vdsReturnValue.getCreationInfo();
            getReturnValue().getInternalTaskIdList().add(
                    CreateTask(taskCreationInfo, getParameters().getParentCommand()));

            // change storage domain in db only if object moved
            if (getParameters().getOperation() == ImageOperation.Move
                    || getParameters().getParentCommand() == VdcActionType.ImportVm
                    || getParameters().getParentCommand() == VdcActionType.ImportVmTemplate) {
                List<DiskImage> snapshots = getDiskImageDao()
                        .getAllSnapshotsForImageGroup(getParameters().getDestImageGroupId());
                setSnapshotForShareableDisk(snapshots);
                for (DiskImage snapshot : snapshots) {
                    getImageStorageDomainMapDao().remove
                            (new image_storage_domain_map_id(snapshot.getImageId(), snapshot.getstorage_ids().get(0)));
                    getImageStorageDomainMapDao().save
                            (new image_storage_domain_map(snapshot.getImageId(), getParameters().getStorageDomainId()));
                }
            } else if (getParameters().getAddImageDomainMapping()) {
                getImageStorageDomainMapDao().save
                        (new image_storage_domain_map(getParameters().getImageId(),
                                getParameters().getStorageDomainId()));
            }

            setSucceeded(true);
        }
    }

    /**
     * Shareable disk which shared between more then one VM, will be returned more then once when fetching the images by image group
     * since it has multiple VM devices (one for each VM it is attached to) and not because he has snapshots,
     * so the shareable disk needs to be distinct when updating the storage domain.
     * @param snapshots - All the images which related to the image group id
     */
    private static void setSnapshotForShareableDisk(List<DiskImage> snapshots) {
        if (!snapshots.isEmpty() && snapshots.get(0).isShareable()) {
            DiskImage sharedDisk = snapshots.get(0);
            snapshots.clear();
            snapshots.add(sharedDisk);
        }
    }

    @Override
    protected SPMAsyncTask ConcreteCreateTask(AsyncTaskCreationInfo asyncTaskCreationInfo, VdcActionType parentCommand) {
        VdcActionParametersBase commandParams = getParametersForTask(parentCommand, getParameters());
        AsyncTaskParameters p = new AsyncTaskParameters(asyncTaskCreationInfo, new async_tasks(parentCommand,
                AsyncTaskResultEnum.success, AsyncTaskStatusEnum.running, asyncTaskCreationInfo.getTaskID(),
                commandParams, asyncTaskCreationInfo.getStepId(), getCommandId()));
        p.setEntityId(getParameters().getEntityId());
        return AsyncTaskManager.getInstance().CreateTask(AsyncTaskType.moveImage, p);
    }

    @Override
    protected void EndWithFailure() {
        if (getMoveOrCopyImageOperation() == ImageOperation.Copy) {
            UnLockImage();
            if (getParameters().getAddImageDomainMapping()) {
                // remove image-storage mapping
                getImageStorageDomainMapDao().remove
                        (new image_storage_domain_map_id(getParameters().getImageId(),
                                getParameters().getStorageDomainId()));
            }
            RevertTasks();
        }

        else {
            MarkImageAsIllegal();
        }

        setSucceeded(true);
    }

    @Override
    protected void RevertTasks() {
        // Revert should be performed only for AddVmFromSnapshot at this point.
        if (getParameters().getParentCommand() == VdcActionType.AddVmFromSnapshot) {
            Guid destImageId = getParameters().getDestinationImageId();
            RemoveImageParameters removeImageParams =
                    new RemoveImageParameters(destImageId);
            removeImageParams.setParentParemeters(getParameters());
            removeImageParams.setParentCommand(VdcActionType.MoveOrCopyImageGroup);
            removeImageParams.setEntityId(getDestinationImageId());
            // Setting the image as the monitored entity, so there will not be dependency
            VdcReturnValueBase returnValue =
                    checkAndPerformRollbackUsingCommand(VdcActionType.RemoveImage, removeImageParams);
            if (returnValue.getSucceeded()) {
                // Starting to monitor the the tasks - RemoveImage is an internal command
                // which adds the taskId on the internal task ID list
                startPollingAsyncTasks(returnValue.getInternalTaskIdList());
            }
        }
    }

    @Override
    protected boolean canPerformRollbackUsingCommand(VdcActionType commandType, VdcActionParametersBase params) {
        return getDiskImageDAO().get(getParameters().getDestinationImageId()) != null;
    }

}
