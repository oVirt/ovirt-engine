package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.RemoveImageParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageOperation;
import org.ovirt.engine.core.common.businessentities.ImageStorageDomainMapId;
import org.ovirt.engine.core.common.businessentities.image_storage_domain_map;
import org.ovirt.engine.core.common.vdscommands.CopyImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.MoveImageGroupVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

@InternalCommandAttribute
public class CopyImageGroupCommand<T extends MoveOrCopyImageGroupParameters> extends BaseImagesCommand<T> {
    public CopyImageGroupCommand(T parameters) {
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
                        getDiskImageDao().getAllSnapshotsForImageGroup(getParameters().getImageGroupID());
                _diskImage = (diskImages.isEmpty()) ? null : diskImages.get(0);
            }

            return _diskImage;

        default:
            return super.getImage();
        }
    }

    @Override
    protected void executeCommand() {
        lockImage();
        VDSReturnValue vdsReturnValue = null;

        Guid sourceDomainId = getParameters().getSourceDomainId() != null ? getParameters().getSourceDomainId()
                : getDiskImage().getStorageIds().get(0);

        Guid taskId = persistAsyncTaskPlaceHolder(getParameters().getParentCommand());

        if (getParameters().getUseCopyCollapse()) {
            vdsReturnValue = runVdsCommand(
                    VDSCommandType.CopyImage,
                    new CopyImageVDSCommandParameters(getStorageDomain().getStoragePoolId(),
                            sourceDomainId,
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
                            isWipeAfterDelete(),
                            getParameters()
                                    .getForceOverride(),
                            getStoragePool().getcompatibility_version().toString()));
        } else {
            vdsReturnValue = runVdsCommand(
                    VDSCommandType.MoveImageGroup,
                    new MoveImageGroupVDSCommandParameters(getDiskImage().getStoragePoolId(),
                            sourceDomainId,
                            getDiskImage()
                                    .getId(),
                            getParameters().getStorageDomainId(),
                            getParameters().getContainerId(),
                            ImageOperation.Copy,
                            isWipeAfterDelete(),
                            getParameters().getForceOverride(),
                            getStoragePool()
                                    .getcompatibility_version().toString()));
        }

        if (vdsReturnValue.getSucceeded()) {
            AsyncTaskCreationInfo taskCreationInfo = vdsReturnValue.getCreationInfo();
            getReturnValue().getInternalVdsmTaskIdList().add(
                    createTask(taskId,
                            taskCreationInfo,
                            getParameters().getParentCommand(),
                            VdcObjectType.Storage,
                            sourceDomainId,
                            getParameters().getStorageDomainId()));

            // Add storage domain in db only if there is new entity in DB.
            if (!shouldUpdateStorageDisk() && getParameters().getAddImageDomainMapping()) {
                getImageStorageDomainMapDao().save
                        (new image_storage_domain_map(getParameters().getImageId(),
                                getParameters().getStorageDomainId()));
            }
            //update quota
            if (getParameters().getQuotaId() != null) {
                getImageDao().updateQuotaForImageAndSnapshots(getParameters().getDestImageGroupId(),
                        getParameters().getQuotaId());
            }

            setSucceeded(true);
        }
    }

    private boolean isWipeAfterDelete() {
        return getDestinationDiskImage() != null ? getDestinationDiskImage().isWipeAfterDelete()
                : getParameters().getWipeAfterDelete();
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
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.moveImage;
    }

    @Override
    protected void endSuccessfully() {
        if (shouldUpdateStorageDisk()) {
            List<DiskImage> snapshots = getDiskImageDao()
                    .getAllSnapshotsForImageGroup(getParameters().getDestImageGroupId());
            setSnapshotForShareableDisk(snapshots);
            for (DiskImage snapshot : snapshots) {
                getImageStorageDomainMapDao().remove
                        (new ImageStorageDomainMapId(snapshot.getImageId(), snapshot.getStorageIds().get(0)));
                getImageStorageDomainMapDao().save
                        (new image_storage_domain_map(snapshot.getImageId(), getParameters().getStorageDomainId()));
            }
        }
        super.endSuccessfully();
    }

    private boolean shouldUpdateStorageDisk() {
        return getParameters().getOperation() == ImageOperation.Move ||
                getParameters().getParentCommand() == VdcActionType.ImportVm;
    }

    @Override
    protected void endWithFailure() {
        if (!getParameters().isImportEntity()) {
            unLockImage();
        }

        if (getParameters().getAddImageDomainMapping()) {
            // remove image-storage mapping
            getImageStorageDomainMapDao().remove
                    (new ImageStorageDomainMapId(getParameters().getImageId(),
                            getParameters().getStorageDomainId()));
        }
        revertTasks();
        setSucceeded(true);
    }

    @Override
    protected void revertTasks() {
        // Revert should be performed only for AddVmFromSnapshot at this point.
        if (getParameters().getParentCommand() == VdcActionType.AddVmFromSnapshot || getParameters().getParentCommand() == VdcActionType.ImportVm
                || getParameters().getParentCommand() == VdcActionType.ImportVmTemplate) {
            Guid destImageId = getParameters().getDestinationImageId();
            RemoveImageParameters removeImageParams =
                    new RemoveImageParameters(destImageId);
            if (getParameters().getParentCommand() == VdcActionType.AddVmFromSnapshot) {
                removeImageParams.setParentParameters(getParameters());
                removeImageParams.setParentCommand(VdcActionType.CopyImageGroup);
            } else {
                removeImageParams.setParentParameters(removeImageParams);
                removeImageParams.setParentCommand(VdcActionType.RemoveImage);
            }
            removeImageParams.setEntityInfo(new EntityInfo(VdcObjectType.Disk, getDestinationImageId()));
            // Setting the image as the monitored entity, so there will not be dependency
            VdcReturnValueBase returnValue =
                    checkAndPerformRollbackUsingCommand(VdcActionType.RemoveImage, removeImageParams);
            if (returnValue.getSucceeded()) {
                // Starting to monitor the the tasks - RemoveImage is an internal command
                // which adds the taskId on the internal task ID list
                startPollingAsyncTasks(returnValue.getInternalVdsmTaskIdList());
            }
        }
    }

    @Override
    protected boolean canPerformRollbackUsingCommand(VdcActionType commandType, VdcActionParametersBase params) {
        return getDiskImageDao().get(getParameters().getDestinationImageId()) != null;
    }

}
