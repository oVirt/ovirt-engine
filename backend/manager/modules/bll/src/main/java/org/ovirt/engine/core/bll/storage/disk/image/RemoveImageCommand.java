package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.domain.PostDeleteActionHandler;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveImageParameters;
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
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskImageDynamicDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * This command responsible to removing image, contains all created snapshots.
 */
@InternalCommandAttribute
@NonTransactiveCommandAttribute(forceCompensation=true)
public class RemoveImageCommand<T extends RemoveImageParameters> extends BaseImagesCommand<T> {

    private static final Set<ActionType> ACTIONS_NOT_REQUIRED_DB_OPERATION = new HashSet<>(
            Arrays.asList(
                    ActionType.RemoveVmFromImportExport,
                    ActionType.RemoveVmTemplateFromImportExport,
                    ActionType.RemoveUnregisteredVmTemplate,
                    ActionType.RemoveUnregisteredVm));

    @Inject
    private PostDeleteActionHandler postDeleteActionHandler;

    @Inject
    protected OvfManager ovfManager;
    @Inject
    private DiskImageDynamicDao diskImageDynamicDao;
    @Inject
    private BaseDiskDao baseDiskDao;
    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private SnapshotDao snapshotDao;
    @Inject
    private ImageStorageDomainMapDao imageStorageDomainMapDao;
    @Inject
    private ImageDao imageDao;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private VmDao vmDao;

    public RemoveImageCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    public void init() {
        super.init();
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

                VDSReturnValue vdsReturnValue = performDeleteImageVdsmOperation();
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
                } else if (e.getErrorCode() == EngineError.ImageDeleteError && isImageRemovedFromStorage()) {
                    // VDSM renames the image before deleting it, so technically the image doesn't exist after renaming,
                    // but the actual delete can still fail with ImageDeleteError.
                    // In this case, Engine has to check whether image still exists on the storage or not.
                    log.info("Disk '{}' was deleted from storage domain '{}'", getDiskImage().getId(),
                            getStorageDomainId());
                } else {
                    throw e;
                }
            }

            if (!ACTIONS_NOT_REQUIRED_DB_OPERATION.contains(getParameters().getParentCommand())) {
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
            return ((List<Guid>) retValue.getReturnValue()).stream().noneMatch(id -> id.equals(getDiskImage().getId()));
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
                        diskImageDynamicDao.remove(diskImage.getImageId());
                        Guid imageTemplate = diskImage.getImageTemplateId();
                        Guid currentGuid = diskImage.getImageId();
                        // next 'while' statement removes snapshots from DB only (the
                        // 'DeleteImageGroup'
                        // VDS Command should take care of removing all the snapshots from
                        // the storage).
                        while (!currentGuid.equals(imageTemplate) && !currentGuid.equals(Guid.Empty)) {
                            removeChildren(currentGuid);

                            DiskImage image = diskImageDao.getSnapshotById(currentGuid);
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

                        baseDiskDao.remove(diskImage.getId());
                        vmDeviceDao.remove(new VmDeviceId(diskImage.getId(), null));

                        updatedSnapshots.forEach(snapshotDao::update);

                        return null;
                    });
        } finally {
            if (getSnapshotsEngineLock() != null) {
                lockManager.releaseLock(getSnapshotsEngineLock());
            }
        }
    }

    /**
     * this method returns the vm that a non shareable disk active snapshot is attached to
     * or null is the disk is unattached to any vm,
     */
    protected VM getVmForNonShareableDiskImage(DiskImage disk) {
        if (!disk.isShareable()) {
            List<VM> vms = vmDao.getVmsListForDisk(disk.getId(), false);
            if (!vms.isEmpty()) {
                return vms.get(0);
            }
        }
        return null;
    }

    private void getImageChildren(Guid snapshot, List<DiskImage> children) {
        List<DiskImage> snapshots = diskImageDao.getAllSnapshotsForParent(snapshot);
        children.addAll(snapshots);
        snapshots.forEach(s -> getImageChildren(s.getId(), children));
    }

    private void removeChildren(Guid snapshot) {
        List<DiskImage> children = new ArrayList<>();
        getImageChildren(snapshot, children);
        Collections.reverse(children);
        children.forEach(this::removeSnapshot);
    }

    /**
     * Prepare a {@link List} of {@link Snapshot} objects with the given disk (image group) removed from it.
     */
    protected List<Snapshot> prepareSnapshotConfigWithoutImage(Guid imageGroupToRemove) {
        List<Snapshot> result = new LinkedList<>();
        List<DiskImage> snapshotDisks = diskImageDao.getAllSnapshotsForImageGroup(imageGroupToRemove);
        for (DiskImage snapshotDisk : snapshotDisks) {
            Guid vmSnapshotId = snapshotDisk.getVmSnapshotId();
            if (vmSnapshotId != null && !Guid.Empty.equals(vmSnapshotId)) {
                Snapshot snapshot = snapshotDao.get(vmSnapshotId);
                Snapshot updated =
                        imagesHandler.prepareSnapshotConfigWithoutImageSingleImage(snapshot,
                                snapshotDisk.getImageId(), ovfManager);
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
            imageStorageDomainMapDao.remove(
                    new ImageStorageDomainMapId(getParameters().getImageId(),
                            getParameters().getStorageDomainId()));
            imageDao.updateStatusOfImagesByImageGroupId(getRelevantDiskImage().getId(),
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

    protected VDSReturnValue performDeleteImageVdsmOperation() {
        if (getParameters().isShouldLockImage()) {
            // the image status should be set to ILLEGAL, so that in case compensation runs the image status will
            // be revert to be ILLEGAL, as we can't tell whether the task started on vdsm side or not.
            imagesHandler.updateAllDiskImageSnapshotsStatusWithCompensation(getRelevantDiskImage().getId(),
                    ImageStatus.LOCKED,
                    ImageStatus.ILLEGAL,
                    getCompensationContext());
        }
        return runVdsCommand(VDSCommandType.DeleteImageGroup,
                postDeleteActionHandler.fixParameters(
                        new DeleteImageGroupVDSCommandParameters(getDiskImage().getStoragePoolId(),
                                getStorageDomainId(), getDiskImage().getId(),
                                getDiskImage().isWipeAfterDelete(), getStorageDomain().getDiscardAfterDelete(),
                                getParameters().isForceDelete())));
    }
}
