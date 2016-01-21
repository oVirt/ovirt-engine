package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.domain.PostZeroHandler;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RemoveImageParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMapId;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.vdscommands.DeleteImageGroupVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetImagesListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * This command responsible to removing image, contains all created snapshots.
 */
@InternalCommandAttribute
@NonTransactiveCommandAttribute(forceCompensation=true)
public class RemoveImageCommand<T extends RemoveImageParameters> extends BaseImagesCommand<T> {

    public RemoveImageCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        initImage();
        initStoragePoolId();
        initStorageDomainId();
    }

    public RemoveImageCommand(Guid commandId) {
        super(commandId);
    }

    protected void initImage() {
        setDiskImage((getParameters().getDiskImage() != null) ? getParameters().getDiskImage() : getImage());
    }

    protected void initStoragePoolId() {
        if (getStoragePoolId() == null || Guid.Empty.equals(getStoragePoolId())) {
            setStoragePoolId(getDiskImage() != null && getDiskImage().getStoragePoolId() != null ? getDiskImage()
                    .getStoragePoolId() : Guid.Empty);
        }
    }

    protected void initStorageDomainId() {
        if ((getParameters().getStorageDomainId() == null || Guid.Empty.equals(getParameters().getStorageDomainId()))
                && getDiskImage() != null) {
            setStorageDomainId(getDiskImage().getStorageIds().get(0));
        }
    }

    @Override
    protected void executeCommand() {
        if (getDiskImage() != null) {
            try {
                Guid taskId = persistAsyncTaskPlaceHolder(getParameters().getParentCommand());

                VDSReturnValue vdsReturnValue = performImageVdsmOperation();
                getTaskIdList().add(
                        createTask(taskId,
                                vdsReturnValue.getCreationInfo(),
                                getParameters().getParentCommand(),
                                VdcObjectType.Storage,
                                getStorageDomainId()));
            } catch (EngineException e) {
                if (e.getErrorCode() == EngineError.ImageDoesNotExistInDomainError) {
                    log.info("Disk '{}' doesn't exist on storage domain '{}', rolling forward",
                            getDiskImage().getId(), getStorageDomainId());
                }
                // VDSM renames the image before deleting it, so technically the image doesn't exist after renaming,
                // but the actual delete can still fail with ImageDeleteError.
                // In this case, Engine has to check whether image still exists on the storage or not.
                else if (e.getErrorCode() == EngineError.ImageDeleteError && isImageRemovedFromStorage()) {
                    log.info("Disk '{}' was deleted from storage domain '{}'", getDiskImage().getId(),
                            getStorageDomainId());
                } else {
                    throw e;
                }
            }

            if (getParameters().getParentCommand() != VdcActionType.RemoveVmFromImportExport
                    && getParameters().getParentCommand() != VdcActionType.RemoveVmTemplateFromImportExport) {
                performImageDbOperations();
            }
        } else {
            log.warn("DiskImage is null, nothing to remove");
        }
        setSucceeded(true);
    }

    protected boolean isImageRemovedFromStorage() {
        VDSReturnValue retValue = runVdsCommand(VDSCommandType.GetImagesList,
            new GetImagesListVDSCommandParameters(getStorageDomainId(), getDiskImage().getStoragePoolId()));

        if (retValue.getSucceeded()) {
            List<Guid> ids = (List<Guid>) retValue.getReturnValue();
            for (Guid id : ids) {
                if (id.equals(getDiskImage().getId())) {
                    return false;
                }
            }
            return true;
        } else {
            log.warn("Could not retrieve image list from storage domain '{}' '{}', disk '{}' might "
                            + "not have been deleted",
                    getStorageDomainId(),
                    getStorageDomain().getName(),
                    getDiskImage().getId());
            return false;
        }
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.deleteImage;
    }

    private void removeImageFromDB() {
        final DiskImage diskImage = getDiskImage();
        final List<Snapshot> updatedSnapshots;

        try {
            VM vm = getVmForNonShareableDiskImage(diskImage);
            // if the disk is not part of a vm (floating), there are no snapshots to update
            // so no lock is required.
            if (getParameters().isRemoveFromSnapshots() && vm != null) {
                lockVmSnapshotsWithWait(vm);
                updatedSnapshots = prepareSnapshotConfigWithoutImage(diskImage.getId());
            } else {
                updatedSnapshots = Collections.emptyList();
            }

            TransactionSupport.executeInScope(TransactionScopeOption.Required,
                    () -> {
                        getDiskImageDynamicDao().remove(diskImage.getImageId());
                        Guid imageTemplate = diskImage.getImageTemplateId();
                        Guid currentGuid = diskImage.getImageId();
                        // next 'while' statement removes snapshots from DB only (the
                        // 'DeleteImageGroup'
                        // VDS Command should take care of removing all the snapshots from
                        // the storage).
                        while (!currentGuid.equals(imageTemplate) && !currentGuid.equals(Guid.Empty)) {
                            removeChildren(currentGuid);

                            DiskImage image = getDiskImageDao().getSnapshotById(currentGuid);
                            if (image != null) {
                                removeSnapshot(image);
                                currentGuid = image.getParentId();
                            } else {
                                currentGuid = Guid.Empty;
                                log.warn(
                                        "'image' (snapshot of image '{}') is null, cannot remove it.",
                                        diskImage.getImageId());
                            }
                        }

                        getBaseDiskDao().remove(diskImage.getId());
                        getVmDeviceDao().remove(new VmDeviceId(diskImage.getId(), null));

                        for (Snapshot s : updatedSnapshots) {
                            getSnapshotDao().update(s);
                        }

                        return null;
                    });
        } finally {
            if (getSnapshotsEngineLock() != null) {
                getLockManager().releaseLock(getSnapshotsEngineLock());
            }
        }
    }

    /**
     * this method returns the vm that a non shareable disk active snapshot is attached to
     * or null is the disk is unattached to any vm,
     */
    protected VM getVmForNonShareableDiskImage(DiskImage disk) {
        if (!disk.isShareable()) {
            List<VM> vms = getVmDao().getVmsListForDisk(disk.getId(), false);
            if (!vms.isEmpty()) {
                return vms.get(0);
            }
        }
        return null;
    }

    private void getImageChildren(Guid snapshot, List<Guid> children) {
        List<Guid> list = new ArrayList<>();
        for (DiskImage image : getDiskImageDao().getAllSnapshotsForParent(snapshot)) {
            list.add(image.getImageId());
        }
        children.addAll(list);
        for (Guid snapshotId : list) {
            getImageChildren(snapshotId, children);
        }
    }

    private void removeChildren(Guid snapshot) {
        List<Guid> children = new ArrayList<>();
        getImageChildren(snapshot, children);
        Collections.reverse(children);
        for (Guid child : children) {
            removeSnapshot(getDiskImageDao().getSnapshotById(child));
        }
    }

    /**
     * Prepare a {@link List} of {@link Snapshot} objects with the given disk (image group) removed from it.
     */
    protected List<Snapshot> prepareSnapshotConfigWithoutImage(Guid imageGroupToRemove) {
        List<Snapshot> result = new LinkedList<>();
        List<DiskImage> snapshotDisks = getDiskImageDao().getAllSnapshotsForImageGroup(imageGroupToRemove);
        for (DiskImage snapshotDisk : snapshotDisks) {
            Guid vmSnapshotId = snapshotDisk.getVmSnapshotId();
            if (vmSnapshotId != null && !Guid.Empty.equals(vmSnapshotId)) {
                Snapshot snapshot = getSnapshotDao().get(vmSnapshotId);
                Snapshot updated =
                        ImagesHandler.prepareSnapshotConfigWithoutImageSingleImage(snapshot,
                                snapshotDisk.getImageId());
                if (updated != null) {
                    result.add(updated);
                }
            }
        }

        return result;
    }

    @Override
    protected void endSuccessfully() {
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        setSucceeded(true);
    }

    private void removeImageMapping() {
        TransactionSupport.executeInNewTransaction(() -> {
            getImageStorageDomainMapDao().remove(
                    new ImageStorageDomainMapId(getParameters().getImageId(),
                            getParameters().getStorageDomainId()));
            ImagesHandler.updateAllDiskImageSnapshotsStatus(getRelevantDiskImage().getId(),
                    getRelevantDiskImage().getImageStatus());
            return null;
        });
    }

    private void performImageDbOperations() {
        switch (getParameters().getDbOperationScope()) {
        case IMAGE:
            removeImageFromDB();
            break;
        case MAPPING:
            removeImageMapping();
            break;
        case NONE:
            break;
        }
    }

    @Override
    protected VDSReturnValue performImageVdsmOperation() {
        if (getParameters().isShouldLockImage()) {
            // the image status should be set to ILLEGAL, so that in case compensation runs the image status will
            // be revert to be ILLEGAL, as we can't tell whether the task started on vdsm side or not.
            ImagesHandler.updateAllDiskImageSnapshotsStatusWithCompensation(getRelevantDiskImage().getId(),
                    ImageStatus.LOCKED,
                    ImageStatus.ILLEGAL,
                    getCompensationContext());
        }
        return runVdsCommand(VDSCommandType.DeleteImageGroup,
                PostZeroHandler.fixParametersWithPostZero(
                        new DeleteImageGroupVDSCommandParameters(getDiskImage().getStoragePoolId(),
                                getStorageDomainId(), getDiskImage().getId(),
                                getDiskImage().isWipeAfterDelete(), getParameters().getForceDelete())));
    }
}
