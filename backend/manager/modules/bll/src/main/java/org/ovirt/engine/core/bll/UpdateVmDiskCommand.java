package org.ovirt.engine.core.bll;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.profiles.DiskProfileHelper;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.DiskValidator;
import org.ovirt.engine.core.bll.validator.LocalizedVmStatus;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ExtendImageSizeParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
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
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.SetVolumeDescriptionVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class UpdateVmDiskCommand<T extends UpdateVmDiskParameters> extends AbstractDiskVmCommand<T>
        implements QuotaStorageDependent {

    private List<PermissionSubject> listPermissionSubjects;
    private final Map<Guid, List<Disk>> otherVmDisks = new HashMap<>();
    private final List<VM> vmsDiskSnapshotPluggedTo = new LinkedList<>();
    private final List<VM> vmsDiskPluggedTo = new LinkedList<>();
    private final List<VM> vmsDiskOrSnapshotPluggedTo = new LinkedList<>();
    private final List<VM> vmsDiskOrSnapshotAttachedTo = new LinkedList<>();

    /**
     * vm device for the given vm and disk
     */
    private VmDevice vmDeviceForVm;
    private Disk oldDisk;

    public UpdateVmDiskCommand(T parameters) {
        super(parameters);
        loadVmDiskAttachedToInfo();
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
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        Map<String, Pair<String, String>> sharedLock = new HashMap<>();

        for (VM vm : vmsDiskOrSnapshotPluggedTo) {
            sharedLock.put(vm.getId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_LOCKED));
        }
        return sharedLock.isEmpty() ? null : sharedLock;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Map<String, Pair<String, String>> exclusiveLock = new HashMap<>();

        if (getNewDisk().isBoot()) {
            for (VM vm : vmsDiskPluggedTo) {
                exclusiveLock.put(vm.getId().toString(),
                        LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_DISK_BOOT, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
            }
        }

        if (resizeDiskImageRequested()) {
            exclusiveLock.put(getOldDisk().getId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK, VdcBllMessages.ACTION_TYPE_FAILED_DISKS_LOCKED));
        }

        return exclusiveLock.isEmpty() ? null : exclusiveLock;
    }

    @Override
    protected void executeVmCommand() {
        ImagesHandler.setDiskAlias(getParameters().getDiskInfo(), getVm());

        if (resizeDiskImageRequested()) {
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

        DiskValidator oldDiskValidator = getDiskValidator(getOldDisk());
        ValidationResult isHostedEngineDisk = oldDiskValidator.validateNotHostedEngineDisk();
        if (!isHostedEngineDisk.isValid()) {
            return validate(isHostedEngineDisk);
        }

        if (!checkDiskUsedAsOvfStore(getOldDisk())) {
            return false;
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        if (!vmsDiskOrSnapshotPluggedTo.isEmpty()) {
            // only virtual drive size can be updated when VMs is running
            if (isAtLeastOneVmIsNotDown(vmsDiskOrSnapshotPluggedTo) && updateParametersRequiringVmDownRequested()) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
            }

            boolean isUpdatedAsBootable = !getOldDisk().isBoot() && getNewDisk().isBoot();
            // multiple boot disk snapshot can be attached to a single vm
            if (isUpdatedAsBootable && !validate(noVmsContainBootableDisks(vmsDiskPluggedTo))) {
                return false;
            }

            boolean isDiskInterfaceUpdated = getOldDisk().getDiskInterface() != getNewDisk().getDiskInterface();
            if (isDiskInterfaceUpdated && !validatePciAndIdeLimit(vmsDiskOrSnapshotPluggedTo)) {
                return false;
            }
        }
        if (DiskStorageType.IMAGE == getOldDisk().getDiskStorageType() && !validateCanResizeDisk()) {
            return false;
        }

        DiskValidator diskValidator = getDiskValidator(getNewDisk());
        return validateCanUpdateShareable() && validateCanUpdateReadOnly(diskValidator) &&
                validateVmPoolProperties() &&
                validate(diskValidator.isVirtIoScsiValid(getVm())) &&
                (getOldDisk().getDiskInterface() == getNewDisk().getDiskInterface()
                || validate(diskValidator.isDiskInterfaceSupported(getVm()))) &&
                setAndValidateDiskProfiles();
    }

    protected StorageDomainValidator getStorageDomainValidator(DiskImage diskImage) {
        StorageDomain storageDomain = getStorageDomainDAO().getForStoragePool(
                diskImage.getStorageIds().get(0), diskImage.getStoragePoolId());
        return new StorageDomainValidator(storageDomain);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_DISK);
    }

    protected boolean validatePciAndIdeLimit(List<VM> vmsDiskPluggedTo) {
        for (VM vm : vmsDiskPluggedTo) {
            List<VmNic> allVmInterfaces = getVmNicDao().getAllForVm(vm.getId());
            List<Disk> allVmDisks = new LinkedList<>(getOtherVmDisks(vm.getId()));
            allVmDisks.add(getNewDisk());

            if (!checkPciAndIdeLimit(vm.getOs(),
                    vm.getVdsGroupCompatibilityVersion(),
                    vm.getNumOfMonitors(),
                    allVmInterfaces,
                    allVmDisks,
                    VmDeviceUtils.isVirtioScsiControllerAttached(vm.getId()),
                    VmDeviceUtils.hasWatchdog(vm.getId()),
                    VmDeviceUtils.isBalloonEnabled(vm.getId()),
                    VmDeviceUtils.isSoundDeviceEnabled(vm.getId()),
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

            StorageDomainStatic sds = getStorageDomainStaticDAO().get(((DiskImage)getNewDisk()).getStorageIds().get(0));
            if (sds.getStorageType() == StorageType.GLUSTERFS) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_SHAREABLE_DISKS_NOT_SUPPORTED_ON_GLUSTER_DOMAIN);
            }

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
            if (vmsDiskOrSnapshotAttachedTo.size() > 1) {
                return failCanDoAction(VdcBllMessages.DISK_IS_ALREADY_SHARED_BETWEEN_VMS);
            }
        }
        return true;
    }

    protected boolean validateCanUpdateReadOnly(DiskValidator diskValidator) {
        if (updateReadOnlyRequested()) {
            if(getVm().getStatus() != VMStatus.Down && vmDeviceForVm.getIsPlugged()) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
            }
            return validate(diskValidator.isReadOnlyPropertyCompatibleWithInterface());
        }
        return true;
    }

    protected boolean validateVmPoolProperties() {
        if ((updateReadOnlyRequested() || updateWipeAfterDeleteRequested()) && getVm().getVmPoolId() != null)
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_ATTACHED_TO_POOL);
        return true;
    }

    protected boolean validateCanResizeDisk() {
        DiskImage newDiskImage = (DiskImage) getNewDisk();
        DiskImage oldDiskImage = (DiskImage) getOldDisk();

        if (newDiskImage.getSize() != oldDiskImage.getSize()) {
            if (Boolean.TRUE.equals(getVmDeviceForVm().getIsReadOnly())) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_RESIZE_READ_ONLY_DISK);
            }

            if (vmDeviceForVm.getSnapshotId() != null) {
                DiskImage snapshotDisk = getDiskImageDao().getDiskSnapshotForVmSnapshot(getParameters().getDiskId(), vmDeviceForVm.getSnapshotId());
                if (snapshotDisk.getSize() != newDiskImage.getSize()) {
                    return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_RESIZE_DISK_SNAPSHOT);
                }
            }

            if (oldDiskImage.getSize() > newDiskImage.getSize()) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_REQUESTED_DISK_SIZE_IS_TOO_SMALL);
            }

            for (VM vm : getVmsDiskPluggedTo()) {
                if (!VdcActionUtils.canExecute(Collections.singletonList(vm), VM.class, VdcActionType.ExtendImageSize)) {
                    return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL, LocalizedVmStatus.from(vm.getStatus()));
                }
            }
            StorageDomainValidator storageDomainValidator = getStorageDomainValidator((DiskImage) getNewDisk());
            if (!validate(storageDomainValidator.isDomainExistAndActive())) {
                return false;
            }

            // For size allocation validation, we'll create a dummy with the additional size required.
            // That way, the validator can hold all the logic about storage types.
            long additionalDiskSpaceInGB = newDiskImage.getSizeInGigabytes() - oldDiskImage.getSizeInGigabytes();
            DiskImage dummyForValidation = DiskImage.copyOf(newDiskImage);
            dummyForValidation.setSizeInGigabytes(additionalDiskSpaceInGB);

            return validate(storageDomainValidator.hasSpaceForNewDisk(dummyForValidation));
        }

        return true;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        if (listPermissionSubjects == null) {
            listPermissionSubjects = new ArrayList<>();

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
        if (shouldPerformMetadataUpdate()) {
            updateMetaDataDescription((DiskImage) getNewDisk());
        }
        final Disk disk = getDiskDao().get(getParameters().getDiskId());
        applyUserChanges(disk);

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
            @Override
            public Object runInTransaction() {
                getVmStaticDAO().incrementDbGeneration(getVm().getId());
                updateDeviceProperties();
                getBaseDiskDao().update(disk);
                if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
                    DiskImage diskImage = (DiskImage) disk;
                    diskImage.setQuotaId(getQuotaId());
                    if (unlockImage && diskImage.getImageStatus() == ImageStatus.LOCKED) {
                        diskImage.setImageStatus(ImageStatus.OK);
                    }
                    getImageDao().update(diskImage.getImage());
                    updateQuota(diskImage);
                    updateDiskProfile();
                }

                reloadDisks();
                updateBootOrder();

                setSucceeded(true);
                return null;
            }

            private void updateDeviceProperties() {
                if (updateReadOnlyRequested()) {
                    vmDeviceForVm.setIsReadOnly(getNewDisk().getReadOnly());
                    getVmDeviceDao().update(vmDeviceForVm);
                }

                if (getOldDisk().getDiskInterface() != getNewDisk().getDiskInterface()) {
                    getVmDeviceDao().clearDeviceAddress(getOldDisk().getId());
                }
            }
        });
    }

    private boolean shouldPerformMetadataUpdate() {
        return ((getNewDisk().getDiskStorageType() == DiskStorageType.IMAGE) && (!ObjectUtils.objectsEqual(getOldDisk().getDiskAlias(),
                getNewDisk().getDiskAlias()) || !ObjectUtils.objectsEqual(getOldDisk().getDiskDescription(),
                getNewDisk().getDiskDescription())));
    }

    private void updateMetaDataDescription(DiskImage diskImage) {
        StorageDomain storageDomain =
                getStorageDomainDAO().getForStoragePool(diskImage.getStorageIds().get(0),
                        getVm().getStoragePoolId());
        if (!getStorageDomainValidator((DiskImage) getNewDisk()).isDomainExistAndActive().isValid()) {
            auditLogForNoMetadataDescriptionUpdate(AuditLogType.UPDATE_DESCRIPTION_FOR_DISK_SKIPPED_SINCE_STORAGE_DOMAIN_NOT_ACTIVE,
                    storageDomain,
                    diskImage);
            return;
        }
        try {
            SetVolumeDescriptionVDSCommandParameters vdsCommandParameters =
                    new SetVolumeDescriptionVDSCommandParameters(getVm().getStoragePoolId(),
                            diskImage.getStorageIds().get(0),
                            diskImage.getId(),
                            diskImage.getImageId(),
                            getJsonDiskDescription());
            runVdsCommand(VDSCommandType.SetVolumeDescription, vdsCommandParameters);
        } catch (Exception e) {
            log.error("Exception while setting volume description for disk. ERROR: '{}'", e);
            auditLogForNoMetadataDescriptionUpdate(AuditLogType.UPDATE_DESCRIPTION_FOR_DISK_FAILED,
                    storageDomain,
                    diskImage);
        }
    }

    private void auditLogForNoMetadataDescriptionUpdate(AuditLogType auditLogType, StorageDomain storageDomain, DiskImage diskImage) {
        AuditLogableBase auditLogableBase = new AuditLogableBase();
        auditLogableBase.addCustomValue("DataCenterName", getStoragePool().getName());
        auditLogableBase.addCustomValue("StorageDomainName", storageDomain.getName());
        auditLogableBase.addCustomValue("DiskName", diskImage.getDiskAlias());
        AuditLogDirector.log(auditLogableBase, auditLogType);
    }

    private String getJsonDiskDescription() throws IOException {
        return ImagesHandler.getJsonDiskDescription(getParameters().getDiskInfo().getDiskAlias(),
                getParameters().getDiskInfo().getDiskDescription());
    }

    protected void updateDiskProfile() {
        if (isDiskImage()) {
            DiskImage oldDisk = (DiskImage) getOldDisk();
            DiskImage newDisk = (DiskImage) getNewDisk();
            if (!Objects.equals(oldDisk.getDiskProfileId(), newDisk.getDiskProfileId())) {
                getImageStorageDomainMapDao().updateDiskProfileByImageGroupIdAndStorageDomainId(newDisk.getId(),
                        newDisk.getStorageIds().get(0),
                        newDisk.getDiskProfileId());
            }
        }
    }

    protected void updateQuota(DiskImage diskImage) {
        if (isDiskImage()) {
            DiskImage oldDisk = (DiskImage) getOldDisk();
            if (!Objects.equals(oldDisk.getQuotaId(), diskImage.getQuotaId())) {
                getImageStorageDomainMapDao().updateQuotaForImageAndSnapshots(diskImage.getId(),
                        diskImage.getStorageIds().get(0),
                        diskImage.getQuotaId());
            }
        }
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
        diskToUpdate.setReadOnly(getNewDisk().getReadOnly());
        diskToUpdate.setSgio(getNewDisk().getSgio());
    }

    protected void reloadDisks() {
        VmHandler.updateDisksFromDb(getVm());
    }

    protected void updateBootOrder() {
        VmDeviceUtils.updateBootOrderInVmDeviceAndStoreToDB(getVm().getStaticData());
    }

    private void extendDiskImageSize() {
        lockImageInDb();

        VdcReturnValueBase ret = runInternalActionWithTasksContext(
                VdcActionType.ExtendImageSize,
                createExtendImageSizeParameters());

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
        VdcReturnValueBase ret = getBackend().endAction(VdcActionType.ExtendImageSize,
                        createExtendImageSizeParameters(),
                getContext().clone().withoutCompensationContext().withoutExecutionContext().withoutLock());

        if (ret.getSucceeded()) {
            performDiskUpdate(true);
        } else {
            unlockImageInDb();
        }

        getReturnValue().setEndActionTryAgain(false);
        setSucceeded(ret.getSucceeded());
    }

    private ValidationResult noVmsContainBootableDisks(List<VM> vms) {
        List<String> vmsWithBoot = new ArrayList<>(vms.size());

        for (VM vm : vms) {
            Disk bootDisk = getDiskDao().getVmBootActiveDisk(vm.getId());
            if (bootDisk != null) {
                vmsWithBoot.add(vm.getName());
            }
        }

        if (!vmsWithBoot.isEmpty()) {
            addCanDoActionMessageVariable("VmsName", StringUtils.join(vmsWithBoot.toArray(), ", "));
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
        getBackend().endAction(VdcActionType.ExtendImageSize,
                params,
                getContext().clone().withoutCompensationContext().withoutExecutionContext().withoutLock());
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

    private boolean isDiskImage() {
        return getOldDisk() != null && getNewDisk() != null && DiskStorageType.IMAGE == getOldDisk().getDiskStorageType();
    }

    protected Guid getQuotaId() {
        if (getNewDisk() != null && isDiskImage()) {
            return ((DiskImage) getNewDisk()).getQuotaId();
        }
        return null;
    }

    protected boolean setAndValidateDiskProfiles() {
        if (isDiskImage()) {
            DiskImage diskImage = (DiskImage) getNewDisk();
            Map<DiskImage, Guid> map = new HashMap<>();
            map.put(diskImage, diskImage.getStorageIds().get(0));
            return validate(DiskProfileHelper.setAndValidateDiskProfiles(map,
                    getStoragePool().getcompatibility_version()));
        }
        return true;
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();

        if (isDiskImage()) {
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

    private boolean resizeDiskImageRequested() {
        return getNewDisk().getDiskStorageType() == DiskStorageType.IMAGE &&
               vmDeviceForVm.getSnapshotId() == null && getNewDisk().getSize() != getOldDisk().getSize();
    }

    private boolean updateParametersRequiringVmDownRequested() {
        return updateDiskParametersRequiringVmDownRequested() || updateImageParametersRequiringVmDownRequested();
    }

    private boolean updateDiskParametersRequiringVmDownRequested() {
        return getOldDisk().isBoot() != getNewDisk().isBoot() ||
                getOldDisk().getDiskInterface() != getNewDisk().getDiskInterface() ||
                getOldDisk().getPropagateErrors() != getNewDisk().getPropagateErrors() ||
                getOldDisk().isShareable() != getNewDisk().isShareable() ||
                getOldDisk().getSgio() != getNewDisk().getSgio();
    }

    private boolean updateImageParametersRequiringVmDownRequested() {
        if (getOldDisk().getDiskStorageType() != DiskStorageType.IMAGE) {
            return false;
        }
        Guid oldQuotaId = ((DiskImage) getOldDisk()).getQuotaId();
        return !Objects.equals(oldQuotaId, getQuotaId());
    }

    protected boolean updateReadOnlyRequested() {
        Boolean readOnlyNewValue = getNewDisk().getReadOnly();
        return readOnlyNewValue != null && !getVmDeviceForVm().getIsReadOnly().equals(readOnlyNewValue);
    }

    protected boolean updateWipeAfterDeleteRequested() {
        return getNewDisk().isWipeAfterDelete() != getOldDisk().isWipeAfterDelete();
    }

    protected boolean isAtLeastOneVmIsNotDown(List<VM> vmsDiskPluggedTo) {
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

    protected Disk getOldDisk() {
        if (oldDisk == null) {
            oldDisk = getDiskDao().get(getParameters().getDiskId());
        }
        return oldDisk;
    }

    private Disk getNewDisk() {
        return getParameters().getDiskInfo();
    }

    protected VmDevice getVmDeviceForVm() {
        return vmDeviceForVm;
    }

    private List<VM> getVmsDiskPluggedTo() {
        return vmsDiskPluggedTo;
    }

    private void loadVmDiskAttachedToInfo() {
        if (getOldDisk() != null) {
            List<Pair<VM, VmDevice>> attachedVmsInfo = getVmDAO().getVmsWithPlugInfo(getOldDisk().getId());
            for (Pair<VM, VmDevice> pair : attachedVmsInfo) {
                VM vm = pair.getFirst();
                vmsDiskOrSnapshotAttachedTo.add(vm);
                if (Boolean.TRUE.equals(pair.getSecond().getIsPlugged())) {
                    if (pair.getSecond().getSnapshotId() != null) {
                        vmsDiskSnapshotPluggedTo.add(vm);
                    } else {
                        vmsDiskPluggedTo.add(vm);
                    }
                    vmsDiskOrSnapshotPluggedTo.add(vm);
                }

                if (vm.getId().equals(getParameters().getVmId())) {
                    vmDeviceForVm = pair.getSecond();
                }
            }
        }
    }

    private void lockImageInDb() {
        final DiskImage diskImage = (DiskImage) getOldDisk();

         TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                getCompensationContext().snapshotEntityStatus(diskImage.getImage());
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
