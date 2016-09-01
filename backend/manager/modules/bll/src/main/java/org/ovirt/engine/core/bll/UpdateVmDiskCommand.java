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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.profiles.DiskProfileHelper;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.LocalizedVmStatus;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ExtendImageSizeParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.SetVolumeDescriptionVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
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

    @Inject
    private VmSlaPolicyUtils vmSlaPolicyUtils;

    @Inject
    private DiskProfileHelper diskProfileHelper;
    /**
     * vm device for the given vm and disk
     */
    private VmDevice vmDeviceForVm;
    private Disk oldDisk;

    public UpdateVmDiskCommand(T parameters) {
        this(parameters, null);
    }

    public UpdateVmDiskCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
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
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, EngineMessage.ACTION_TYPE_FAILED_VM_IS_LOCKED));
        }
        return sharedLock.isEmpty() ? null : sharedLock;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Map<String, Pair<String, String>> exclusiveLock = new HashMap<>();

        if (getNewDisk().isBoot()) {
            for (VM vm : vmsDiskPluggedTo) {
                exclusiveLock.put(vm.getId().toString(),
                        LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_DISK_BOOT, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
            }
        }

        if (resizeDiskImageRequested()) {
            exclusiveLock.put(getOldDisk().getId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK, EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED));
        }

        return exclusiveLock.isEmpty() ? null : exclusiveLock;
    }

    @Override
    protected void executeVmCommand() {
        ImagesHandler.setDiskAlias(getParameters().getDiskInfo(), getVm());

        if (resizeDiskImageRequested()) {
            switch (getOldDisk().getDiskStorageType()) {
                case IMAGE:
                    extendDiskImageSize();
                    break;
                case CINDER:
                    extendCinderDiskSize();
                    break;
            }
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

        boolean isDiskImageOrCinder = DiskStorageType.IMAGE == getOldDisk().getDiskStorageType() ||
                DiskStorageType.CINDER == getOldDisk().getDiskStorageType();

        if (isDiskImageOrCinder) {
            ValidationResult imagesNotLocked =
                    new DiskImagesValidator(Collections.singletonList((DiskImage) getOldDisk())).diskImagesNotLocked();
            if (!imagesNotLocked.isValid()) {
                return validate(imagesNotLocked);
            }
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

        boolean isDiskInterfaceUpdated = getOldDisk().getDiskInterface() != getNewDisk().getDiskInterface();
        if (!vmsDiskOrSnapshotPluggedTo.isEmpty()) {
            // only virtual drive size can be updated when VMs is running
            if (isAtLeastOneVmIsNotDown(vmsDiskOrSnapshotPluggedTo) && updateParametersRequiringVmDownRequested()) {
                return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
            }

            boolean isUpdatedAsBootable = !getOldDisk().isBoot() && getNewDisk().isBoot();
            // multiple boot disk snapshot can be attached to a single vm
            if (isUpdatedAsBootable && !validate(noVmsContainBootableDisks(vmsDiskPluggedTo))) {
                return false;
            }

            if (isDiskInterfaceUpdated && !validatePciAndIdeLimit(vmsDiskOrSnapshotPluggedTo)) {
                return false;
            }
        }
        if (isDiskImageOrCinder && !validateCanResizeDisk()) {
            return false;
        }

        DiskValidator diskValidator = getDiskValidator(getNewDisk());
        return validateCanUpdateShareable() && validateCanUpdateReadOnly(diskValidator) &&
                validateVmPoolProperties() &&
                validate(diskValidator.isVirtIoScsiValid(getVm())) &&
                (!isDiskInterfaceUpdated || validate(diskValidator.isDiskInterfaceSupported(getVm()))) &&
                setAndValidateDiskProfiles();
    }

    protected StorageDomainValidator getStorageDomainValidator(DiskImage diskImage) {
        StorageDomain storageDomain = getStorageDomainDao().getForStoragePool(
                diskImage.getStorageIds().get(0), diskImage.getStoragePoolId());
        return new StorageDomainValidator(storageDomain);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(EngineMessage.VAR__ACTION__UPDATE);
        addCanDoActionMessage(EngineMessage.VAR__TYPE__VM_DISK);
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
                    VmDeviceUtils.hasVirtioScsiController(vm.getId()),
                    VmDeviceUtils.hasWatchdog(vm.getId()),
                    VmDeviceUtils.hasMemoryBalloon(vm.getId()),
                    VmDeviceUtils.hasSoundDevice(vm.getId()),
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

            StorageDomainStatic sds = getStorageDomainStaticDao().get(((DiskImage)getNewDisk()).getStorageIds().get(0));
            if (sds.getStorageType() == StorageType.GLUSTERFS) {
                return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_SHAREABLE_DISKS_NOT_SUPPORTED_ON_GLUSTER_DOMAIN);
            }

            List<DiskImage> diskImageList =
                    getDiskImageDao().getAllSnapshotsForImageGroup(getOldDisk().getId());

            // If disk image list is more than one then we assume that it has a snapshot, since one image is the active
            // disk and all the other images are the snapshots.
            if ((diskImageList.size() > 1) || !Guid.Empty.equals(((DiskImage) getOldDisk()).getImageTemplateId())) {
                return failCanDoAction(EngineMessage.SHAREABLE_DISK_IS_NOT_SUPPORTED_FOR_DISK);
            }

            if (!isVersionSupportedForShareable(getOldDisk(), getStoragePoolDao().get(getVm().getStoragePoolId())
                    .getCompatibilityVersion()
                    .getValue())) {
                return failCanDoAction(EngineMessage.ACTION_NOT_SUPPORTED_FOR_CLUSTER_POOL_LEVEL);
            }

            if (!isVolumeFormatSupportedForShareable(((DiskImage) getNewDisk()).getVolumeFormat())) {
                return failCanDoAction(EngineMessage.SHAREABLE_DISK_IS_NOT_SUPPORTED_BY_VOLUME_FORMAT);
            }
        } else if (isUpdatedToNonShareable(getOldDisk(), getNewDisk())) {
            if (vmsDiskOrSnapshotAttachedTo.size() > 1) {
                return failCanDoAction(EngineMessage.DISK_IS_ALREADY_SHARED_BETWEEN_VMS);
            }
        }
        return true;
    }

    protected boolean validateCanUpdateReadOnly(DiskValidator diskValidator) {
        if (updateReadOnlyRequested()) {
            if(getVm().getStatus() != VMStatus.Down && vmDeviceForVm.getIsPlugged()) {
                return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
            }
            return validate(diskValidator.isReadOnlyPropertyCompatibleWithInterface());
        }
        return true;
    }

    protected boolean validateVmPoolProperties() {
        if ((updateReadOnlyRequested() || updateWipeAfterDeleteRequested()) && getVm().getVmPoolId() != null)
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_VM_ATTACHED_TO_POOL);
        return true;
    }

    protected boolean validateCanResizeDisk() {
        DiskImage newDiskImage = (DiskImage) getNewDisk();
        DiskImage oldDiskImage = (DiskImage) getOldDisk();

        if (newDiskImage.getSize() != oldDiskImage.getSize()) {
            if (Boolean.TRUE.equals(getVmDeviceForVm().getIsReadOnly())) {
                return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_CANNOT_RESIZE_READ_ONLY_DISK);
            }

            if (vmDeviceForVm.getSnapshotId() != null) {
                DiskImage snapshotDisk = getDiskImageDao().getDiskSnapshotForVmSnapshot(getParameters().getDiskId(), vmDeviceForVm.getSnapshotId());
                if (snapshotDisk.getSize() != newDiskImage.getSize()) {
                    return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_CANNOT_RESIZE_DISK_SNAPSHOT);
                }
            }

            if (oldDiskImage.getSize() > newDiskImage.getSize()) {
                return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_REQUESTED_DISK_SIZE_IS_TOO_SMALL);
            }

            for (VM vm : getVmsDiskPluggedTo()) {
                if (!VdcActionUtils.canExecute(Collections.singletonList(vm), VM.class, VdcActionType.ExtendImageSize)) {
                    return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL, LocalizedVmStatus.from(vm.getStatus()));
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

    protected void performDiskUpdate(final boolean unlockImage) {
        if (shouldPerformMetadataUpdate()) {
            updateMetaDataDescription((DiskImage) getNewDisk());
        }
        final Disk disk = getDiskDao().get(getParameters().getDiskId());
        applyUserChanges(disk);

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
            @Override
            public Object runInTransaction() {
                getVmStaticDao().incrementDbGeneration(getVm().getId());
                updateDeviceProperties();
                getBaseDiskDao().update(disk);
                switch (disk.getDiskStorageType()) {
                    case IMAGE:
                        DiskImage diskImage = (DiskImage) disk;
                        diskImage.setQuotaId(getQuotaId());
                        if (unlockImage && diskImage.getImageStatus() == ImageStatus.LOCKED) {
                            diskImage.setImageStatus(ImageStatus.OK);
                        }
                        getImageDao().update(diskImage.getImage());
                        updateQuota(diskImage);
                        updateDiskProfile();
                        break;
                    case CINDER:
                        CinderDisk cinderDisk = (CinderDisk) disk;
                        cinderDisk.setQuotaId(getQuotaId());
                        setStorageDomainId(cinderDisk.getStorageIds().get(0));
                        getCinderBroker().updateDisk(cinderDisk);
                        if (unlockImage && cinderDisk.getImageStatus() == ImageStatus.LOCKED) {
                            cinderDisk.setImageStatus(ImageStatus.OK);
                        }
                        getImageDao().update(cinderDisk.getImage());
                        updateQuota(cinderDisk);
                        break;
                    case LUN:
                        updateLunProperties((LunDisk)getNewDisk());
                        break;
                }

                reloadDisks();
                updateBootOrder();

                setSucceeded(true);
                liveUpdateDiskProfile();
                return null;
            }

            private void updateDeviceProperties() {
                if (updateReadOnlyRequested()) {
                    vmDeviceForVm.setIsReadOnly(getNewDisk().getReadOnly());
                    getVmDeviceDao().update(vmDeviceForVm);
                }

                if (getOldDisk().getDiskInterface() != getNewDisk().getDiskInterface()) {
                    vmDeviceForVm.setAddress("");
                    getVmDeviceDao().clearDeviceAddress(getOldDisk().getId());
                }
            }

            private void updateLunProperties(LunDisk lunDisk) {
                if (updateIsUsingScsiReservationRequested(lunDisk)) {
                    vmDeviceForVm.setUsingScsiReservation(lunDisk.isUsingScsiReservation());
                    getVmDeviceDao().update(vmDeviceForVm);
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
                getStorageDomainDao().getForStoragePool(diskImage.getStorageIds().get(0),
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
        auditLogDirector.log(auditLogableBase, auditLogType);
    }

    private String getJsonDiskDescription() throws IOException {
        return MetadataDiskDescriptionHandler.getInstance().generateJsonDiskDescription(getParameters().getDiskInfo());
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

    private void liveUpdateDiskProfile() {
        if (isDiskImage()) {
            DiskImage oldDisk = (DiskImage) getOldDisk();
            DiskImage newDisk = (DiskImage) getNewDisk();
            if (!Objects.equals(oldDisk.getDiskProfileId(), newDisk.getDiskProfileId())) {
                vmSlaPolicyUtils.refreshRunningVmsWithDiskProfile(newDisk.getDiskProfileId());
            }
        }
    }

    protected void updateQuota(DiskImage diskImage) {
        if (isInternalManagedDisk()) {
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
        VmDeviceUtils.updateBootOrder(getVm().getId());
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

    private void extendCinderDiskSize() {
        lockImageInDb();
        CinderDisk newCinderDisk = (CinderDisk) getNewDisk();
        Future<VdcReturnValueBase> future = CommandCoordinatorUtil.executeAsyncCommand(
                VdcActionType.ExtendCinderDisk,
                buildExtendCinderDiskParameters(newCinderDisk),
                cloneContextAndDetachFromParent(),
                new SubjectEntity(VdcObjectType.Storage, newCinderDisk.getStorageIds().get(0)));
        addCustomValue("NewSize", String.valueOf(getNewDiskSizeInGB()));
        try {
            setReturnValue(future.get());
            setSucceeded(getReturnValue().getSucceeded());
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error extending Cinder disk '{}': {}",
                    getNewDisk().getDiskAlias(),
                    e.getMessage());
            log.debug("Exception", e);
        }
    }

    private VdcActionParametersBase buildExtendCinderDiskParameters(CinderDisk newCinderDisk) {
        UpdateVmDiskParameters parameters = new UpdateVmDiskParameters(
                getVmId(), newCinderDisk.getId(), newCinderDisk);
        parameters.setParametersCurrentUser(getParameters().getParametersCurrentUser());
        return parameters;
    }

    @Override
    protected void endSuccessfully() {
        if (!isDiskImage()) {
            return;
        }

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
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VMS_BOOT_IN_USE);
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
        if (getSucceeded()) {
            return isCinderDisk() && resizeDiskImageRequested() ?
                    AuditLogType.USER_EXTENDED_DISK_SIZE : AuditLogType.USER_UPDATE_VM_DISK;
        } else {
            return AuditLogType.USER_FAILED_UPDATE_VM_DISK;
        }
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

    public long getNewDiskSizeInGB() {
        CinderDisk cinderDisk = (CinderDisk) getNewDisk();
        return cinderDisk.getSize() / BYTES_IN_GB;
    }

    private boolean isDiskImage() {
        return isDiskStorageType(DiskStorageType.IMAGE);
    }

    private boolean isCinderDisk() {
        return isDiskStorageType(DiskStorageType.CINDER);
    }

    private boolean isDiskStorageType(DiskStorageType diskStorageType) {
        return getOldDisk() != null && getNewDisk() != null && diskStorageType == getOldDisk().getDiskStorageType();
    }

    protected Guid getQuotaId() {
        if (getNewDisk() != null && isInternalManagedDisk()) {
            return ((DiskImage) getNewDisk()).getQuotaId();
        }
        return null;
    }

    protected boolean setAndValidateDiskProfiles() {
        if (isDiskImage()) {
            DiskImage diskImage = (DiskImage) getNewDisk();
            // when disk profile isn't updated, skip check.
            if (diskImage.getDiskProfileId() != null
                    && diskImage.getDiskProfileId().equals(((DiskImage) getOldDisk()).getDiskProfileId())) {
                return true;
            }
            Map<DiskImage, Guid> map = new HashMap<>();
            map.put(diskImage, diskImage.getStorageIds().get(0));
            return validate(diskProfileHelper.setAndValidateDiskProfiles(map,
                    getStoragePool().getCompatibilityVersion(), getCurrentUser()));
        }
        return true;
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();

        if (isInternalManagedDisk()) {
            DiskImage oldDiskImage = (DiskImage) getOldDisk();
            DiskImage newDiskImage = (DiskImage) getNewDisk();

            boolean emptyOldQuota = oldDiskImage.getQuotaId() == null || Guid.Empty.equals(oldDiskImage.getQuotaId());
            boolean differentNewQuota = !emptyOldQuota && !oldDiskImage.getQuotaId().equals(newDiskImage.getQuotaId());
            long diskExtendingDiff = newDiskImage.getSizeInGigabytes() - oldDiskImage.getSizeInGigabytes();

            if (emptyOldQuota || differentNewQuota ) {
                list.add(generateQuotaConsumeParameters(newDiskImage, newDiskImage.getSizeInGigabytes()));
            } else if (diskExtendingDiff > 0L) {
                list.add(generateQuotaConsumeParameters(newDiskImage, diskExtendingDiff));
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

    protected boolean isInternalManagedDisk() {
        return isDiskImage() || isCinderDisk();
    }

    private QuotaConsumptionParameter generateQuotaConsumeParameters(DiskImage newDiskImage, long sizeInGigabytes) {
        return new QuotaStorageConsumptionParameter(
               newDiskImage.getQuotaId(),
               null,
               QuotaStorageConsumptionParameter.QuotaAction.CONSUME,
               //TODO: Shared Disk?
               newDiskImage.getStorageIds().get(0),
               (double) sizeInGigabytes );
    }

    private boolean resizeDiskImageRequested() {
        boolean sizeChanged = getNewDisk().getSize() != getOldDisk().getSize();
        switch (getNewDisk().getDiskStorageType()) {
            case IMAGE:
                return sizeChanged && vmDeviceForVm.getSnapshotId() == null;
            case CINDER:
                return sizeChanged;
        }
        return false;
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

    /**
     * Command's canDoAction conditions: requiring all connected VMs down.
     * @return true - if disk type is IMAGE or is CINDER, and updating quota
     */
    private boolean updateImageParametersRequiringVmDownRequested() {
        if (!getOldDisk().getDiskStorageType().isInternal()) {
            return false;
        }
        Guid oldQuotaId = ((DiskImage) getOldDisk()).getQuotaId();
        /*
         * oldQuotaId == null : Initial quota, not assigned yet.
         * happens when: quota is disabled or,
         * quota enabled, but disk never attached with a quota
         */
        if (oldQuotaId == null) {
            return false;
        }
        return !Objects.equals(oldQuotaId, getQuotaId());
    }

    protected boolean updateReadOnlyRequested() {
        Boolean readOnlyNewValue = getNewDisk().getReadOnly();
        return readOnlyNewValue != null && !getVmDeviceForVm().getIsReadOnly().equals(readOnlyNewValue);
    }

    private boolean updateIsUsingScsiReservationRequested(LunDisk lunDisk) {
        Boolean isUsingScsiReservationNewValue = lunDisk.isUsingScsiReservation();
        return isUsingScsiReservationNewValue != null &&
               getVmDeviceForVm().isUsingScsiReservation() != isUsingScsiReservationNewValue;
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

    protected Disk getNewDisk() {
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
            List<Pair<VM, VmDevice>> attachedVmsInfo = getVmDao().getVmsWithPlugInfo(getOldDisk().getId());
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

    public void unlockImageInDb() {
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
