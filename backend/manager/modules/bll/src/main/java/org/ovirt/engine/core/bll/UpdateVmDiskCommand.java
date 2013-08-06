package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.DiskValidator;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ExtendImageSizeParameters;
import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@LockIdNameAttribute(isReleaseAtEndOfExecute = false)
@NonTransactiveCommandAttribute(forceCompensation = true)
public class UpdateVmDiskCommand<T extends UpdateVmDiskParameters> extends AbstractDiskVmCommand<T>
        implements QuotaStorageDependent {

    private List<PermissionSubject> listPermissionSubjects;
    private Map<Guid, List<Disk>> otherVmDisks = new HashMap<Guid, List<Disk>>();
    private List<VM> vmsDiskPluggedTo;
    private Disk oldDisk;

    public UpdateVmDiskCommand(T parameters) {
        super(parameters);
    }

    /**
     * This constructor is mandatory for activation of the compensation process
     * after the server restart.
     * @param commandId
     */
    public UpdateVmDiskCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        Map<String, Pair<String, String>> sharedLock = new HashMap<String, Pair<String, String>>();
        List<VM> vmsDiskPluggedTo = getVmsDiskPluggedTo();

        for (VM vm : vmsDiskPluggedTo) {
            sharedLock.put(vm.getId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_LOCKED));
        }
        return sharedLock.isEmpty() ? null : sharedLock;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Map<String, Pair<String, String>> exclusiveLock = new HashMap<String, Pair<String, String>>();
        List<VM> vmsDiskPluggedTo = getVmsDiskPluggedTo();

        if (getNewDisk().isBoot()) {
            for (VM vm : vmsDiskPluggedTo) {
                exclusiveLock.put(vm.getId().toString(),
                        LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_DISK_BOOT, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
            }
        }

        if (shouldResizeDiskImage()) {
            exclusiveLock.put(getOldDisk().getId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK, VdcBllMessages.ACTION_TYPE_FAILED_DISKS_LOCKED));
        }

        return exclusiveLock.isEmpty() ? null : exclusiveLock;
    }

    @Override
    protected void executeVmCommand() {
        ImagesHandler.setDiskAlias(getParameters().getDiskInfo(), getVm());

        if (shouldResizeDiskImage()) {
            extendDiskImageSize();
        } else {
            try {
                performDiskUpdate(false);
            } finally {
                freeLock();
            }
        }
    }

    @Override
    protected boolean canDoAction() {
        if (!isVmExist() || !isDiskExist(getOldDisk())) {
            return false;
        }

        List<VM> vmsDiskPluggedTo = getVmsDiskPluggedTo();
        if (vmsDiskPluggedTo != null && !vmsDiskPluggedTo.isEmpty()) {
            // only virtual drive size can be updated when VMs is running
            if (isAtLeastOneVmIsNotDown(vmsDiskPluggedTo) && shouldUpdatePropertiesOtherThanSize()) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
            }

            boolean isUpdatedAsBootable = !getOldDisk().isBoot() && getNewDisk().isBoot();
            if (isUpdatedAsBootable && !validate(noVmsContainBootableDisks(vmsDiskPluggedTo))) {
                return false;
            }

            boolean isDiskInterfaceUpdated = getOldDisk().getDiskInterface() != getNewDisk().getDiskInterface();
            if (isDiskInterfaceUpdated && !validatePciAndIdeLimit(vmsDiskPluggedTo)) {
                return false;
            }
        }

        if (shouldResizeDiskImage() && !validateCanResizeDisk()) {
            return false;
        }

        DiskValidator diskValidator = new DiskValidator(getNewDisk());
        return validateCanUpdateShareable() && validate(diskValidator.isVirtIoScsiValid(getVm()));
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_DISK);
    }

    private boolean validatePciAndIdeLimit(List<VM> vmsDiskPluggedTo) {
        for (VM vm : vmsDiskPluggedTo) {
            List<VmNetworkInterface> allVmInterfaces = getVmNetworkInterfaceDao().getAllForVm(vm.getId());
            List<Disk> allVmDisks = new LinkedList<Disk>(getOtherVmDisks(vm.getId()));
            allVmDisks.add(getNewDisk());

            if (!checkPciAndIdeLimit(vm.getNumOfMonitors(), allVmInterfaces, allVmDisks,
                    getReturnValue().getCanDoActionMessages())) {
                return false;
            }
        }
        return true;
    }

    protected List<Disk> getOtherVmDisks(Guid vmId) {
        List<Disk> disks = otherVmDisks.get(vmId);
        if (disks == null) {
            disks = getDiskDao().getAllForVm(vmId);
            Iterator<Disk> iter = disks.iterator();
            while (iter.hasNext()) {
                Disk evalDisk = iter.next();
                if (evalDisk.getId().equals(getOldDisk().getId())) {
                    iter.remove();
                    break;
                }
            }
            otherVmDisks.put(vmId, disks);
        }
        return disks;
    }

    /**
     * Validate whether a disk can be shareable. Disk can be shareable if it is not based on qcow FS,
     * which means it should not be based on a template image with thin provisioning,
     * it also should not contain snapshots and it is not bootable.
     * @return Indication whether the disk can be shared or not.
     */
    private boolean validateCanUpdateShareable() {
        if (DiskStorageType.LUN == getOldDisk().getDiskStorageType()) {
            return true;
        }

        // Check if VM is not during snapshot.
        if (!isVmNotInPreviewSnapshot()) {
            return false;
        }

        if (isUpdatedToShareable(getOldDisk(), getNewDisk())) {
            List<DiskImage> diskImageList =
                    getDiskImageDao().getAllSnapshotsForImageGroup(getOldDisk().getId());

            // If disk image list is more then one then we assume that it has a snapshot, since one image is the active
            // disk and all the other images are the snapshots.
            if ((diskImageList.size() > 1) || !Guid.Empty.equals(((DiskImage) getOldDisk()).getImageTemplateId())) {
                return failCanDoAction(VdcBllMessages.SHAREABLE_DISK_IS_NOT_SUPPORTED_FOR_DISK);
            }

            if (!isVersionSupportedForShareable(getOldDisk(), getStoragePoolDAO().get(getVm().getStoragePoolId())
                    .getcompatibility_version()
                    .getValue())) {
                return failCanDoAction(VdcBllMessages.ACTION_NOT_SUPPORTED_FOR_CLUSTER_POOL_LEVEL);
            }

            if (!isVolumeFormatSupportedForShareable(((DiskImage) getNewDisk()).getVolumeFormat())) {
                return failCanDoAction(VdcBllMessages.SHAREABLE_DISK_IS_NOT_SUPPORTED_BY_VOLUME_FORMAT);
            }
        } else if (isUpdatedToNonShareable(getOldDisk(), getNewDisk())) {
            if (getVmDAO().getVmsListForDisk(getOldDisk().getId()).size() > 1) {
                return failCanDoAction(VdcBllMessages.DISK_IS_ALREADY_SHARED_BETWEEN_VMS);
            }
        }
        return true;
    }

    private boolean validateCanResizeDisk() {
        DiskImage oldDiskImage = (DiskImage) getOldDisk();
        DiskImage newDiskImage = (DiskImage) getNewDisk();

        if (oldDiskImage.getSize() > newDiskImage.getSize()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_REQUESTED_DISK_SIZE_IS_TOO_SMALL);
        }

        if (!VdcActionUtils.CanExecute(getVmsDiskPluggedTo(), VM.class, VdcActionType.ExtendImageSize)) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL);
        }

        long additionalDiskSpaceInGB = newDiskImage.getSizeInGigabytes() - oldDiskImage.getSizeInGigabytes();
        StorageDomain storageDomain = getStorageDomainDAO().getForStoragePool(
                newDiskImage.getStorageIds().get(0), newDiskImage.getStoragePoolId());
        StorageDomainValidator storageDomainValidator = new StorageDomainValidator(storageDomain);

        return validate(storageDomainValidator.isDomainExistAndActive()) &&
                validate(storageDomainValidator.isDomainHasSpaceForRequest(additionalDiskSpaceInGB));
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        if (listPermissionSubjects == null) {
            listPermissionSubjects = new ArrayList<PermissionSubject>();

            Guid diskId = (getOldDisk() == null) ? null : getOldDisk().getId();
            listPermissionSubjects.add(new PermissionSubject(diskId,
                    VdcObjectType.Disk,
                    ActionGroup.EDIT_DISK_PROPERTIES));

            if (getOldDisk() != null && getNewDisk() != null && getOldDisk().getSgio() != getNewDisk().getSgio()) {
                listPermissionSubjects.add(new PermissionSubject(diskId,
                        VdcObjectType.Disk,
                        ActionGroup.CONFIGURE_SCSI_GENERIC_IO));
            }
        }
        return listPermissionSubjects;
    }

    private void performDiskUpdate(final boolean unlockImage) {
        final Disk disk = getDiskDao().get(getParameters().getDiskId());
        applyUserChanges(disk);

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
            @Override
            public Object runInTransaction() {
                getVmStaticDAO().incrementDbGeneration(getVm().getId());
                clearAddressOnInterfaceChange(disk, getNewDisk());
                getBaseDiskDao().update(disk);
                if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
                    DiskImage diskImage = (DiskImage) disk;
                    diskImage.setQuotaId(getQuotaId());
                    if (unlockImage && diskImage.getImageStatus() == ImageStatus.LOCKED) {
                        diskImage.setImageStatus(ImageStatus.OK);
                    }
                    getImageDao().update(diskImage.getImage());
                }
                updateVmDisksAndDevice();

                setSucceeded(true);
                return null;
            }

            private void clearAddressOnInterfaceChange(Disk diskToUpdate, Disk diskWithUserChanges) {
                // clear the disk address if the type has changed
                if (diskToUpdate.getDiskInterface() != diskWithUserChanges.getDiskInterface()) {
                    getVmDeviceDao().clearDeviceAddress(getVmDeviceDao().get(new VmDeviceId(diskToUpdate.getId(),
                            getVmId())).getDeviceId());
                }
            }
        });
    }

    private void applyUserChanges(Disk diskToUpdate) {
        updateSnapshotIdOnShareableChange(diskToUpdate, getNewDisk());
        diskToUpdate.setBoot(getNewDisk().isBoot());
        diskToUpdate.setDiskInterface(getNewDisk().getDiskInterface());
        diskToUpdate.setPropagateErrors(getNewDisk().getPropagateErrors());
        diskToUpdate.setWipeAfterDelete(getNewDisk().isWipeAfterDelete());
        diskToUpdate.setDiskAlias(getNewDisk().getDiskAlias());
        diskToUpdate.setDiskDescription(getNewDisk().getDiskDescription());
        diskToUpdate.setShareable(getNewDisk().isShareable());
        diskToUpdate.setSgio(getNewDisk().getSgio());
    }

    protected void updateVmDisksAndDevice() {
        // update cached image
        VmHandler.updateDisksFromDb(getVm());
        // update vm device boot order
        VmDeviceUtils.updateBootOrderInVmDeviceAndStoreToDB(getVm().getStaticData());
    }

    private void extendDiskImageSize() {
        lockImageInDb();

        VdcReturnValueBase ret = getBackend().runInternalAction(
                VdcActionType.ExtendImageSize,
                createExtendImageSizeParameters(),
                ExecutionHandler.createDefaultContexForTasks(getExecutionContext())
        );

        if (ret.getSucceeded()) {
            getReturnValue().getVdsmTaskIdList().addAll(ret.getInternalVdsmTaskIdList());
        } else {
            propagateInternalCommandFailure(ret);
            getReturnValue().setFault(ret.getFault());
        }
        setSucceeded(ret.getSucceeded());
    }

    @Override
    protected void endSuccessfully() {
        VdcReturnValueBase ret = getBackend().EndAction(VdcActionType.ExtendImageSize,
                createExtendImageSizeParameters());

        if (ret.getSucceeded()) {
            performDiskUpdate(true);
        } else {
            unlockImageInDb();
        }

        getReturnValue().setEndActionTryAgain(false);
        setSucceeded(ret.getSucceeded());
    }

    private ValidationResult noVmsContainBootableDisks(List<VM> vms) {
        List<String> vmsWithBoot = new ArrayList<String>(vms.size());

        for (VM vm : vms) {
            Disk bootDisk = getDiskDao().getVmBootDisk(vm.getId());
            if (bootDisk != null) {
                vmsWithBoot.add(vm.getName());
            }
        }

        if (!vmsWithBoot.isEmpty()) {
            addCanDoActionMessage(String.format("$VmsName %1$s", StringUtils.join(vmsWithBoot.toArray(), ", ")));
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VMS_BOOT_IN_USE);
        }

        return ValidationResult.VALID;
    }

    @Override
    protected void endWithFailure() {
        endInternalCommandWithFailure();
        unlockImageInDb();
        getReturnValue().setEndActionTryAgain(false);
        setSucceeded(true);
    }

    private void endInternalCommandWithFailure() {
        ExtendImageSizeParameters params = createExtendImageSizeParameters();
        params.setTaskGroupSuccess(false);
        getBackend().EndAction(VdcActionType.ExtendImageSize, params);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_VM_DISK : AuditLogType.USER_FAILED_UPDATE_VM_DISK;
    }

    @Override
    public String getDiskAlias() {
        return getOldDisk().getDiskAlias();
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put("diskalias", getDiskAlias());
        }
        return jobProperties;
    }

    private boolean isQuotaValidationNeeded() {
        return getOldDisk() != null && getNewDisk() != null && DiskStorageType.IMAGE == getOldDisk().getDiskStorageType();
    }

    protected Guid getQuotaId() {
        if (getNewDisk() != null && isQuotaValidationNeeded()) {
            return ((DiskImage) getNewDisk()).getQuotaId();
        }
        return null;
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();

        if (isQuotaValidationNeeded()) {
            DiskImage oldDiskImage = (DiskImage) getOldDisk();
            DiskImage newDiskImage = (DiskImage) getNewDisk();

            boolean emptyOldQuota = oldDiskImage.getQuotaId() == null || Guid.Empty.equals(oldDiskImage.getQuotaId());
            boolean differentNewQuota = !emptyOldQuota && !oldDiskImage.getQuotaId().equals(newDiskImage.getQuotaId());

            if (emptyOldQuota || differentNewQuota) {
                list.add(new QuotaStorageConsumptionParameter(
                        newDiskImage.getQuotaId(),
                        null,
                        QuotaStorageConsumptionParameter.QuotaAction.CONSUME,
                        //TODO: Shared Disk?
                        newDiskImage.getStorageIds().get(0),
                        (double)newDiskImage.getSizeInGigabytes()));
            }

            if (differentNewQuota) {
                list.add(new QuotaStorageConsumptionParameter(
                        oldDiskImage.getQuotaId(),
                        null,
                        QuotaStorageConsumptionParameter.QuotaAction.RELEASE,
                        //TODO: Shared Disk?
                        oldDiskImage.getStorageIds().get(0),
                        (double)oldDiskImage.getSizeInGigabytes()));
            }
        }
        return list;
    }

    private boolean shouldResizeDiskImage() {
        return getNewDisk().getDiskStorageType() == DiskStorageType.IMAGE &&
                getNewDisk().getSize() != getOldDisk().getSize();
    }

    private boolean shouldUpdatePropertiesOtherThanSize() {
        return shouldUpdateDiskProperties() || shouldUpdateImageProperties();
    }

    private boolean shouldUpdateDiskProperties() {
        return getOldDisk().isBoot() != getNewDisk().isBoot() ||
                getOldDisk().getDiskInterface() != getNewDisk().getDiskInterface() ||
                getOldDisk().getPropagateErrors() != getNewDisk().getPropagateErrors() ||
                getOldDisk().isWipeAfterDelete() != getNewDisk().isWipeAfterDelete() ||
                getOldDisk().isShareable() != getNewDisk().isShareable() ||
                getOldDisk().getSgio() != getNewDisk().getSgio() ||
                !StringUtils.equals(getOldDisk().getDiskDescription(), getNewDisk().getDiskDescription()) ||
                !StringUtils.equals(getOldDisk().getDiskAlias(), getNewDisk().getDiskAlias());
    }

    private boolean shouldUpdateImageProperties() {
        return (getOldDisk().getDiskStorageType() == DiskStorageType.IMAGE) &&
                !((DiskImage) getOldDisk()).getQuotaId().equals(getQuotaId());
    }

    private boolean isAtLeastOneVmIsNotDown(List<VM> vmsDiskPluggedTo) {
        for (VM vm : vmsDiskPluggedTo) {
            if (vm.getStatus() != VMStatus.Down) {
                return true;
            }
        }
        return false;
    }

    private boolean isUpdatedToShareable(Disk oldDisk, Disk newDisk) {
        return newDisk.isShareable() && !oldDisk.isShareable();
    }

    private boolean isUpdatedToNonShareable(Disk oldDisk, Disk newDisk) {
        return !newDisk.isShareable() && oldDisk.isShareable();
    }

    private void updateSnapshotIdOnShareableChange(Disk oldDisk, Disk newDisk) {
        if (oldDisk.isShareable() != newDisk.isShareable() && oldDisk.getDiskStorageType() == DiskStorageType.IMAGE) {
            DiskImage oldDiskImage = (DiskImage) oldDisk;
            Guid vmSnapshotId = isUpdatedToShareable(oldDisk, newDisk) ? null :
                    getSnapshotDao().getId(getVmId(), SnapshotType.ACTIVE);
            oldDiskImage.setVmSnapshotId(vmSnapshotId);
        }
    }

    private Disk getOldDisk() {
        if (oldDisk == null) {
            oldDisk = getDiskDao().get(getParameters().getDiskId());
        }
        return oldDisk;
    }

    private Disk getNewDisk() {
        return getParameters().getDiskInfo();
    }

    private List<VM> getVmsDiskPluggedTo() {
        if (vmsDiskPluggedTo == null) {
            vmsDiskPluggedTo = getVmDAO().getForDisk(getOldDisk().getId()).get(Boolean.TRUE);

            if (vmsDiskPluggedTo == null) {
                vmsDiskPluggedTo = Collections.emptyList();
            }
        }

        return vmsDiskPluggedTo;
    }

    private void lockImageInDb() {
        final DiskImage diskImage = (DiskImage) getOldDisk();
        final ImageStatus imgStatus = diskImage.getImageStatus();

         TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                getCompensationContext().snapshotEntityStatus(diskImage.getImage(), imgStatus);
                getCompensationContext().stateChanged();
                diskImage.setImageStatus(ImageStatus.LOCKED);
                ImagesHandler.updateImageStatus(diskImage.getImageId(), ImageStatus.LOCKED);
                return null;
            }
         });
    }

    private void unlockImageInDb() {
        final DiskImage diskImage = (DiskImage) getOldDisk();
        diskImage.setImageStatus(ImageStatus.OK);
        ImagesHandler.updateImageStatus(diskImage.getImageId(), ImageStatus.OK);
    }

    private ExtendImageSizeParameters createExtendImageSizeParameters() {
        DiskImage diskImage = (DiskImage) getNewDisk();
        ExtendImageSizeParameters params = new ExtendImageSizeParameters(diskImage.getImageId(), diskImage.getSize());
        params.setStoragePoolId(diskImage.getStoragePoolId());
        params.setStorageDomainId(diskImage.getStorageIds().get(0));
        params.setImageGroupID(diskImage.getId());
        params.setParentCommand(VdcActionType.UpdateVmDisk);
        params.setParentParameters(getParameters());
        return params;
    }

    private void propagateInternalCommandFailure(VdcReturnValueBase internalReturnValue) {
        getReturnValue().getExecuteFailedMessages().clear();
        getReturnValue().getExecuteFailedMessages().addAll(internalReturnValue.getExecuteFailedMessages());
        getReturnValue().setFault(internalReturnValue.getFault());
        getReturnValue().getCanDoActionMessages().clear();
        getReturnValue().getCanDoActionMessages().addAll(internalReturnValue.getCanDoActionMessages());
        getReturnValue().setCanDoAction(internalReturnValue.getCanDoAction());
    }
}
