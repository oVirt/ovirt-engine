package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RemoveImageParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.ImageStorageDomainMapId;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.vdscommands.DeleteImageGroupVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * This command responsible to removing image, contains all created snapshots.
 */
@SuppressWarnings("serial")
@InternalCommandAttribute
@NonTransactiveCommandAttribute(forceCompensation=true)
public class RemoveImageCommand<T extends RemoveImageParameters> extends BaseImagesCommand<T> {
    private EngineLock snapshotsEngineLock;

    public RemoveImageCommand(T parameters) {
        super(parameters);
        initImage();
        initStoragePoolId();
        initStorageDomainId();
    }

    protected RemoveImageCommand(Guid commandId) {
        super(commandId);
    }

    protected void initImage() {
        setDiskImage(((getParameters().getDiskImage()) != null) ? getParameters().getDiskImage() : getImage());
    }

    protected void initStoragePoolId() {
        if (getStoragePoolId() == null || Guid.Empty.equals(getStoragePoolId())) {
            setStoragePoolId(getDiskImage() != null && getDiskImage().getstorage_pool_id() != null ? getDiskImage()
                    .getstorage_pool_id().getValue() : Guid.Empty);
        }
    }

    protected void initStorageDomainId() {
        if ((getParameters().getStorageDomainId() == null || Guid.Empty.equals(getParameters().getStorageDomainId()))
                && getDiskImage() != null) {
            setStorageDomainId(getDiskImage().getstorage_ids().get(0));
        }
    }

    @Override
    protected void executeCommand() {
        if (getDiskImage() != null) {
            VDSReturnValue vdsReturnValue = performImageVdsmOperation();
            getReturnValue().getInternalTaskIdList().add(
                    createTask(vdsReturnValue.getCreationInfo(),
                            getParameters().getParentCommand(),
                            VdcObjectType.Storage,
                            getParameters().getStorageDomainId()));

            if (getParameters().isRemoveDuringExecution()
                    && getParameters().getParentCommand() != VdcActionType.RemoveVmFromImportExport
                    && getParameters().getParentCommand() != VdcActionType.RemoveVmTemplateFromImportExport) {
                removeImageFromDB(false);
            }
        } else {
            log.warn("DiskImage is null, nothing to remove");
        }
        setSucceeded(true);
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.deleteImage;
    }

    private void removeImageFromDB(boolean isLockOnSnapshotsNeeded) {
        final DiskImage diskImage = getDiskImage();
        final List<Snapshot> updatedSnapshots;

        try {
            VM vm = getVmForNonShareableDiskImage(diskImage);
            // if the disk is not part of a vm (floating), there are no snapshots to update
            // so no lock is required.
            if (isLockOnSnapshotsNeeded && vm!=null) {
                lockVmSnapshotsWithWait(vm);
                updatedSnapshots = prepareSnapshotConfigWithoutImage(diskImage.getId());
            } else {
                updatedSnapshots = Collections.emptyList();
            }

            TransactionSupport.executeInScope(TransactionScopeOption.Required,
                    new TransactionMethod<Object>() {
                        @Override
                        public Object runInTransaction() {
                            getDiskImageDynamicDAO().remove(diskImage.getImageId());
                            Guid imageTemplate = diskImage.getit_guid();
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
                                    log.warnFormat(
                                            "'image' (snapshot of image '{0}') is null, cannot remove it.",
                                            diskImage.getImageId());
                                }
                            }

                            getBaseDiskDao().remove(diskImage.getId());
                            getVmDeviceDAO().remove(new VmDeviceId(diskImage.getId(), null));

                            for (Snapshot s : updatedSnapshots) {
                                getSnapshotDao().update(s);
                            }

                            return null;
                        }
                    });
        } finally {
            if (snapshotsEngineLock != null) {
                getLockManager().releaseLock(snapshotsEngineLock);
            }
        }
    }

    private void lockVmSnapshotsWithWait(VM vm) {
        snapshotsEngineLock = new EngineLock();
        Map<String, String> snapshotsExlusiveLockMap = Collections.singletonMap(vm.getId().toString(), LockingGroup.VM_SNAPSHOTS.name());
        snapshotsEngineLock.setExclusiveLocks(snapshotsExlusiveLockMap);
        getLockManager().acquireLockWait(snapshotsEngineLock);
    }

    /**
     * this method returns the vm that a non shareable disk is attached to
     * or null is the disk is unattached to any vm,
     * @param disk
     * @return
     */
    private VM getVmForNonShareableDiskImage(DiskImage disk) {
        if (!disk.isShareable()) {
            List<VM> vms = getVmDAO().getVmsListForDisk(disk.getId());
            if (!vms.isEmpty()) {
                return vms.get(0);
            }
        }
        return null;
    }

    private void getImageChildren(Guid snapshot, List<Guid> children) {
        List<Guid> list = new ArrayList<Guid>();
        for (DiskImage image : getDiskImageDao().getAllSnapshotsForParent(snapshot)) {
            list.add(image.getImageId());
        }
        children.addAll(list);
        for (Guid snapshotId : list) {
            getImageChildren(snapshotId, children);
        }
    }

    private void removeChildren(Guid snapshot) {
        List<Guid> children = new ArrayList<Guid>();
        getImageChildren(snapshot, children);
        Collections.reverse(children);
        for (Guid child : children) {
            removeSnapshot(getDiskImageDao().getSnapshotById(child));
        }
    }

    /**
     * Prepare a {@link List} of {@link Snapshot} objects with the given disk (image group) removed from it.
     */
    private List<Snapshot> prepareSnapshotConfigWithoutImage(Guid imageGroupToRemove) {
        List<Snapshot> result = new LinkedList<Snapshot>();
        List<DiskImage> snapshotDisks = getDiskImageDao().getAllSnapshotsForImageGroup(imageGroupToRemove);
        for (DiskImage snapshotDisk : snapshotDisks) {
            NGuid vmSnapshotId = snapshotDisk.getvm_snapshot_id();
            if (vmSnapshotId != null && !Guid.Empty.equals(vmSnapshotId.getValue())) {
                Snapshot updated =
                        prepareSnapshotConfigWithoutImageSingleImage(vmSnapshotId.getValue(),
                                snapshotDisk.getImageId());
                if (updated != null) {
                    result.add(updated);
                }
            }
        }

        return result;
    }

    /**
     * Prepare a single {@link Snapshot} object representing a snapshot of a given VM without the give disk.
     */
    protected Snapshot prepareSnapshotConfigWithoutImageSingleImage(Guid vmSnapshotId, Guid imageId) {
        Snapshot snap = null;
        try {
            OvfManager ovfManager = new OvfManager();
            snap = getSnapshotDao().get(vmSnapshotId);
            String snapConfig = snap.getVmConfiguration();

            if (snap.isVmConfigurationAvailable() && snapConfig != null) {
                VM vmSnapshot = new VM();
                ArrayList<DiskImage> snapshotImages = new ArrayList<DiskImage>();

                ovfManager.ImportVm(snapConfig,
                        vmSnapshot,
                        snapshotImages,
                        new ArrayList<VmNetworkInterface>());

                // Remove the image form the disk list
                Iterator<DiskImage> diskIter = snapshotImages.iterator();
                while (diskIter.hasNext()) {
                    DiskImage imageInList = diskIter.next();
                    if (imageInList.getImageId().equals(imageId)) {
                        log.debugFormat("Recreating vmSnapshot {0} without the image {1}", vmSnapshotId, imageId);
                        diskIter.remove();
                        break;
                    }
                }

                String newOvf = ovfManager.ExportVm(vmSnapshot, snapshotImages);
                snap.setVmConfiguration(newOvf);
            }
        } catch (OvfReaderException e) {
            log.errorFormat("Can't remove image {0} from snapshot {1}", imageId, vmSnapshotId);
        }
        return snap;
    }

    @Override
    protected void endSuccessfully() {
        endCommand();
    }

    @Override
    protected void endWithFailure() {
        endCommand();
    }

    private void endCommand() {
        if (getParameters().getRemoveFromDB()) {
            if (!getParameters().isRemoveDuringExecution()) {
                removeImageFromDB(true);
            }
        } else {
            getImageStorageDomainMapDao().remove(
                    new ImageStorageDomainMapId(getParameters().getImageId(),
                            getParameters().getStorageDomainId()));
            unLockImage();
        }

        setSucceeded(true);
    }

    @Override
    protected VDSReturnValue performImageVdsmOperation() {
        boolean isShouldBeLocked = getParameters().getParentCommand() != VdcActionType.RemoveVmFromImportExport
                && getParameters().getParentCommand() != VdcActionType.RemoveVmTemplateFromImportExport;
        if (isShouldBeLocked) {
            // the image status should be set to ILLEGAL, so that in case compensation runs the image status will
            // be revert to be ILLEGAL, as we can't tell whether the task started on vdsm side or not.
            getDiskImage().setImageStatus(ImageStatus.ILLEGAL);
            lockImageWithCompensation();
        }
        // Releasing the lock for cases it was set by the parent command. The lock can be released because the image
        // status was already changed to lock.
        freeLock();
        VDSReturnValue returnValue = runVdsCommand(VDSCommandType.DeleteImageGroup,
                new DeleteImageGroupVDSCommandParameters(getDiskImage().getstorage_pool_id().getValue(),
                        getStorageDomainId().getValue(), getDiskImage().getId()
                                .getValue(), getDiskImage().isWipeAfterDelete(), getParameters()
                                .getForceDelete(), getStoragePool().getcompatibility_version().toString()));
        return returnValue;
    }

    protected VmDeviceDAO getVmDeviceDAO() {
        return getDbFacade().getVmDeviceDao();
    }
}
