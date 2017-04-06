package org.ovirt.engine.core.bll.storage.disk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VmSlaPolicyUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.profiles.DiskProfileHelper;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.storage.disk.image.MetadataDiskDescriptionHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.LocalizedVmStatus;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskVmElementValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AmendImageGroupVolumesCommandParameters;
import org.ovirt.engine.core.common.action.ExtendImageSizeParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmDiskOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.common.vdscommands.SetVolumeDescriptionVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class UpdateVmDiskCommand<T extends VmDiskOperationParameterBase> extends AbstractDiskVmCommand<T>
        implements QuotaStorageDependent {

    /* Multiplier used to convert GB to bytes or vice versa. */
    private static final long BYTES_IN_GB = 1024 * 1024 * 1024;

    private List<PermissionSubject> listPermissionSubjects;
    private final List<VM> vmsDiskSnapshotPluggedTo = new LinkedList<>();
    private final List<VM> vmsDiskPluggedTo = new LinkedList<>();
    private final List<VM> vmsDiskOrSnapshotPluggedTo = new LinkedList<>();
    private final List<VM> vmsDiskOrSnapshotAttachedTo = new LinkedList<>();

    @Inject
    VmSlaPolicyUtils vmSlaPolicyUtils;
    @Inject
    private DiskProfileHelper diskProfileHelper;
    @Inject
    private VmDao vmDao;

    /**
     * vm device for the given vm and disk
     */
    private VmDevice vmDeviceForVm;
    private Disk oldDisk;
    private DiskVmElement oldDiskVmElement;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private StorageDomainStaticDao storageDomainStaticDao;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private DiskDao diskDao;
    @Inject
    private DiskVmElementDao diskVmElementDao;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private BaseDiskDao baseDiskDao;
    @Inject
    private ImageDao imageDao;
    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private ImageStorageDomainMapDao imageStorageDomainMapDao;
    @Inject
    private SnapshotDao snapshotDao;

    public UpdateVmDiskCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void init() {
        super.init();
        loadVmDiskAttachedToInfo();
    }

    /**
     * This constructor is mandatory for activation of the compensation process
     * after the server restart.
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

        if (getDiskVmElement() != null && getDiskVmElement().isBoot()) {
            exclusiveLock.put(getParameters().getVmId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_DISK_BOOT, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
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
                if (Objects.equals(getOldDisk().getDiskStorageType(), DiskStorageType.IMAGE) && amendDiskRequested()) {
                    amendDiskImage();
                }
            } finally {
                freeLock();
            }
        }
    }

    @Override
    protected boolean validate() {
        if (!validate(new VmValidator(getVm()).isVmExists()) || !isDiskExistAndAttachedToVm(getOldDisk()) ||
                !validateDiskVmData()) {
            return false;
        }

        boolean isDiskImageOrCinder = DiskStorageType.IMAGE == getOldDisk().getDiskStorageType() ||
                DiskStorageType.CINDER == getOldDisk().getDiskStorageType();

        if (isDiskImageOrCinder) {
            ValidationResult imagesNotLocked = new DiskImagesValidator((DiskImage) getOldDisk()).diskImagesNotLocked();
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

        boolean isDiskInterfaceUpdated = getOldDiskVmElement().getDiskInterface() != getDiskVmElement().getDiskInterface();
        if (!vmsDiskOrSnapshotPluggedTo.isEmpty()) {
            // only virtual drive size can be updated when VMs is running
            if (isAtLeastOneVmIsNotDown(vmsDiskOrSnapshotPluggedTo) && updateParametersRequiringVmDownRequested()) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
            }

            boolean isUpdatedAsBootable = !getOldDiskVmElement().isBoot() && getDiskVmElement().isBoot();
            // multiple boot disk snapshot can be attached to a single vm
            if (isUpdatedAsBootable && !validate(oldDiskValidator.isVmNotContainsBootDisk(getVm()))) {
                return false;
            }

            if (isDiskInterfaceUpdated && !isDiskPassPciAndIdeLimit()) {
                return false;
            }
        }
        if (isDiskImageOrCinder && !validateCanResizeDisk()) {
            return false;
        }

        if (resizeDiskImageRequested() && amendDiskRequested()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_AMEND_AND_EXTEND_IN_ONE_OPERATION);
        }

        DiskVmElementValidator diskVmElementValidator = getDiskVmElementValidator(getNewDisk(), getDiskVmElement());
        return validateCanUpdateShareable() && validateCanUpdateReadOnly() &&
                validateVmPoolProperties() && validateQuota() &&
                validate(diskVmElementValidator.isVirtIoScsiValid(getVm())) &&
                (!isDiskInterfaceUpdated || validate(diskVmElementValidator.isDiskInterfaceSupported(getVm()))) &&
                setAndValidateDiskProfiles() &&
                validatePassDiscardSupported(diskVmElementValidator);
    }

    private boolean validatePassDiscardSupported(DiskVmElementValidator diskVmElementValidator) {
        if (!FeatureSupported.passDiscardSupported(getStoragePool().getCompatibilityVersion()) &&
                getOldDiskVmElement().isPassDiscard() != getDiskVmElement().isPassDiscard()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_DC_VERSION,
                    String.format("$dataCenterVersion %s", getStoragePool().getCompatibilityVersion().toString()));
        }
        Guid storageDomainId = getNewDisk().getDiskStorageType() == DiskStorageType.IMAGE ?
                ((DiskImage) getNewDisk()).getStorageIds().get(0) : null;
        return validate(diskVmElementValidator.isPassDiscardSupported(storageDomainId));
    }

    protected StorageDomainValidator getStorageDomainValidator(DiskImage diskImage) {
        StorageDomain storageDomain = storageDomainDao.getForStoragePool(
                diskImage.getStorageIds().get(0), diskImage.getStoragePoolId());
        return new StorageDomainValidator(storageDomain);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
        addValidationMessage(EngineMessage.VAR__TYPE__DISK);
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

            StorageDomainStatic sds = storageDomainStaticDao.get(((DiskImage)getNewDisk()).getStorageIds().get(0));
            if (sds.getStorageType() == StorageType.GLUSTERFS) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_SHAREABLE_DISKS_NOT_SUPPORTED_ON_GLUSTER_DOMAIN);
            }

            List<DiskImage> diskImageList =
                    diskImageDao.getAllSnapshotsForImageGroup(getOldDisk().getId());

            // If disk image list is more than one then we assume that it has a snapshot, since one image is the active
            // disk and all the other images are the snapshots.
            if ((diskImageList.size() > 1) || !Guid.Empty.equals(((DiskImage) getOldDisk()).getImageTemplateId())) {
                return failValidation(EngineMessage.SHAREABLE_DISK_IS_NOT_SUPPORTED_FOR_DISK);
            }

            if (!isVolumeFormatSupportedForShareable(((DiskImage) getNewDisk()).getVolumeFormat())) {
                return failValidation(EngineMessage.SHAREABLE_DISK_IS_NOT_SUPPORTED_BY_VOLUME_FORMAT);
            }
        } else if (isUpdatedToNonShareable(getOldDisk(), getNewDisk())) {
            if (vmsDiskOrSnapshotAttachedTo.size() > 1) {
                return failValidation(EngineMessage.DISK_IS_ALREADY_SHARED_BETWEEN_VMS);
            }
        }
        return true;
    }

    protected boolean validateCanUpdateReadOnly() {
        if (updateReadOnlyRequested()) {
            if(getVm().getStatus() != VMStatus.Down && vmDeviceForVm.isPlugged()) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
            }
            DiskVmElementValidator diskVmElementValidator = getDiskVmElementValidator(getNewDisk(), getDiskVmElement());
            return validate(diskVmElementValidator.isReadOnlyPropertyCompatibleWithInterface());
        }
        return true;
    }

    protected boolean validateVmPoolProperties() {
        if ((updateReadOnlyRequested() || updateWipeAfterDeleteRequested()) && getVm().getVmPoolId() != null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_ATTACHED_TO_POOL);
        }
        return true;
    }

    protected boolean validateCanResizeDisk() {
        DiskImage newDiskImage = (DiskImage) getNewDisk();
        DiskImage oldDiskImage = (DiskImage) getOldDisk();

        if (newDiskImage.getSize() != oldDiskImage.getSize()) {
            if (Boolean.TRUE.equals(getVmDeviceForVm().getReadOnly())) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_CANNOT_RESIZE_READ_ONLY_DISK);
            }

            if (vmDeviceForVm.getSnapshotId() != null) {
                DiskImage snapshotDisk = diskImageDao.getDiskSnapshotForVmSnapshot(getParameters().getDiskInfo().getId(), vmDeviceForVm.getSnapshotId());
                if (snapshotDisk.getSize() != newDiskImage.getSize()) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_CANNOT_RESIZE_DISK_SNAPSHOT);
                }
            }

            if (oldDiskImage.getSize() > newDiskImage.getSize()) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_REQUESTED_DISK_SIZE_IS_TOO_SMALL);
            }

            for (VM vm : getVmsDiskPluggedTo()) {
                if (!VdcActionUtils.canExecute(Collections.singletonList(vm), VM.class, VdcActionType.ExtendImageSize)) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL, LocalizedVmStatus.from(vm.getStatus()));
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
        final Disk diskForUpdate = diskDao.get(getParameters().getDiskInfo().getId());
        final DiskVmElement diskVmElementForUpdate = diskVmElementDao.get(new VmDeviceId(getOldDisk().getId(), getVmId()));

        applyUserChanges(diskForUpdate, diskVmElementForUpdate);

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
            @Override
            public Object runInTransaction() {
                vmStaticDao.incrementDbGeneration(getVm().getId());
                updateDeviceProperties();
                baseDiskDao.update(diskForUpdate);
                diskVmElementDao.update(diskVmElementForUpdate);
                switch (diskForUpdate.getDiskStorageType()) {
                    case IMAGE:
                        DiskImage diskImage = (DiskImage) diskForUpdate;
                        diskImage.setQuotaId(getQuotaId());
                        if (unlockImage && diskImage.getImageStatus() == ImageStatus.LOCKED) {
                            diskImage.setImageStatus(ImageStatus.OK);
                        }
                        imageDao.update(diskImage.getImage());
                        updateQuota(diskImage);
                        updateDiskProfile();
                        break;
                    case CINDER:
                        CinderDisk cinderDisk = (CinderDisk) diskForUpdate;
                        cinderDisk.setQuotaId(getQuotaId());
                        setStorageDomainId(cinderDisk.getStorageIds().get(0));
                        getCinderBroker().updateDisk(cinderDisk);
                        if (unlockImage && cinderDisk.getImageStatus() == ImageStatus.LOCKED) {
                            cinderDisk.setImageStatus(ImageStatus.OK);
                        }
                        imageDao.update(cinderDisk.getImage());
                        updateQuota(cinderDisk);
                        break;
                    case LUN:
                        // No specific update for LUN disk
                        break;
                }

                reloadDisks();

                setSucceeded(true);
                // If necessary set the new Storage QoS values on running VMs asynchronously
                liveUpdateDiskProfile();
                return null;
            }

            private void updateDeviceProperties() {
                if (updateReadOnlyRequested()) {
                    vmDeviceForVm.setReadOnly(getDiskVmElement().isReadOnly());
                    vmDeviceDao.update(vmDeviceForVm);
                }

                if ((getOldDiskVmElement().getDiskInterface() != getDiskVmElement().getDiskInterface()) ||
                        ((getOldDiskVmElement().isBoot() != getDiskVmElement().isBoot()) && (
                                getDiskVmElement().getDiskInterface() == DiskInterface.IDE))) {
                    vmDeviceForVm.setAddress("");
                    vmDeviceDao.clearDeviceAddress(getOldDisk().getId());
                }
            }
        });
    }

    private boolean shouldPerformMetadataUpdate() {
        return (getNewDisk().getDiskStorageType() == DiskStorageType.IMAGE) &&
                (!Objects.equals(getOldDisk().getDiskAlias(), getNewDisk().getDiskAlias()) ||
                 !Objects.equals(getOldDisk().getDiskDescription(), getNewDisk().getDiskDescription()));
    }

    private void updateMetaDataDescription(DiskImage diskImage) {
        StorageDomain storageDomain =
                storageDomainDao.getForStoragePool(diskImage.getStorageIds().get(0), getVm().getStoragePoolId());
        if (!getStorageDomainValidator((DiskImage) getNewDisk()).isDomainExistAndActive().isValid()) {
            auditLogForNoMetadataDescriptionUpdate(AuditLogType.UPDATE_DESCRIPTION_FOR_DISK_SKIPPED_SINCE_STORAGE_DOMAIN_NOT_ACTIVE,
                    storageDomain,
                    diskImage);
            return;
        }
        setVolumeDescription(diskImage, storageDomain);
    }

    protected void setVolumeDescription(DiskImage diskImage, StorageDomain storageDomain) {
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
        addCustomValue("DataCenterName", getStoragePool().getName());
        addCustomValue("StorageDomainName", storageDomain.getName());
        addCustomValue("DiskName", diskImage.getDiskAlias());
        auditLogDirector.log(this, auditLogType);
    }

    private String getJsonDiskDescription() throws IOException {
        return MetadataDiskDescriptionHandler.getInstance().generateJsonDiskDescription(getParameters().getDiskInfo());
    }

    protected void updateDiskProfile() {
        if (isDiskImage()) {
            DiskImage oldDisk = (DiskImage) getOldDisk();
            DiskImage newDisk = (DiskImage) getNewDisk();
            if (!Objects.equals(oldDisk.getDiskProfileId(), newDisk.getDiskProfileId())) {
                imageStorageDomainMapDao.updateDiskProfileByImageGroupIdAndStorageDomainId(newDisk.getId(),
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
                imageStorageDomainMapDao.updateQuotaForImageAndSnapshots(diskImage.getId(),
                        diskImage.getStorageIds().get(0),
                        diskImage.getQuotaId());
            }
        }
    }

    private void applyUserChanges(Disk diskToUpdate, DiskVmElement dveToUpdate) {
        updateSnapshotIdOnShareableChange(diskToUpdate, getNewDisk());
        diskToUpdate.setPropagateErrors(getNewDisk().getPropagateErrors());
        diskToUpdate.setWipeAfterDelete(getNewDisk().isWipeAfterDelete());
        diskToUpdate.setDiskAlias(getNewDisk().getDiskAlias());
        diskToUpdate.setDiskDescription(getNewDisk().getDiskDescription());
        diskToUpdate.setShareable(getNewDisk().isShareable());
        diskToUpdate.setSgio(getNewDisk().getSgio());

        dveToUpdate.setBoot(getDiskVmElement().isBoot());
        dveToUpdate.setDiskInterface(getDiskVmElement().getDiskInterface());
        dveToUpdate.setPassDiscard(getDiskVmElement().isPassDiscard());
        dveToUpdate.setUsingScsiReservation(getDiskVmElement().isUsingScsiReservation());
    }

    protected void reloadDisks() {
        vmHandler.updateDisksFromDb(getVm());
    }

    private void extendDiskImageSize() {
        lockImageInDb();

        VdcReturnValueBase ret = runInternalActionWithTasksContext(
                VdcActionType.ExtendImageSize,
                createExtendImageSizeParameters());

        if (!ret.getSucceeded()) {
            propagateInternalCommandFailure(ret);
            getReturnValue().setFault(ret.getFault());
        }
        getReturnValue().getVdsmTaskIdList().addAll(ret.getInternalVdsmTaskIdList());
        setSucceeded(ret.getSucceeded());
    }

    protected void amendDiskImage() {
        VdcReturnValueBase ret = runInternalActionWithTasksContext(VdcActionType.AmendImageGroupVolumes,
                amendImageGroupVolumesCommandParameters());

        if (!ret.getSucceeded()) {
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
                cloneContextAndDetachFromParent());
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
        VmDiskOperationParameterBase parameters = new VmDiskOperationParameterBase(
                DiskVmElement.copyOf(getOldDiskVmElement()), newCinderDisk);
        parameters.setParametersCurrentUser(getParameters().getParametersCurrentUser());
        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
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

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
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
            Guid quotaId = ((DiskImage) getNewDisk()).getQuotaId();
            return getQuotaManager().getDefaultQuotaIfNull(quotaId, getStoragePoolId());
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
            return validate(diskProfileHelper.setAndValidateDiskProfiles(map, getCurrentUser()));
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

    protected boolean amendDiskRequested() {
        if (getNewDisk().getDiskStorageType() == DiskStorageType.IMAGE) {
            DiskImage oldDisk = (DiskImage) getOldDisk();
            return (oldDisk.getVolumeFormat() == VolumeFormat.COW)
                    && !Objects.equals(oldDisk.getQcowCompat().getCompatValue(),
                            ((DiskImage) getNewDisk()).getQcowCompat().getCompatValue());
        }
        return false;
    }

    private boolean updateParametersRequiringVmDownRequested() {
        return updateDiskParametersRequiringVmDownRequested() || updateImageParametersRequiringVmDownRequested();
    }

    private boolean updateDiskParametersRequiringVmDownRequested() {
        return getOldDiskVmElement().isBoot() != getDiskVmElement().isBoot() ||
                getOldDiskVmElement().getDiskInterface() != getDiskVmElement().getDiskInterface() ||
                getOldDisk().getPropagateErrors() != getNewDisk().getPropagateErrors() ||
                getOldDisk().isShareable() != getNewDisk().isShareable() ||
                getOldDisk().getSgio() != getNewDisk().getSgio();
    }

    /**
     * Command's validate conditions: requiring all connected VMs down.
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
        boolean readOnlyNewValue = getDiskVmElement().isReadOnly();
        return !getVmDeviceForVm().getReadOnly().equals(readOnlyNewValue);
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
                    snapshotDao.getId(getVmId(), SnapshotType.ACTIVE);
            oldDiskImage.setVmSnapshotId(vmSnapshotId);
        }
    }

    protected Disk getOldDisk() {
        if (oldDisk == null && getParameters().getDiskInfo() != null) {
            oldDisk = diskDao.get(getParameters().getDiskInfo().getId());
        }
        return oldDisk;
    }

    protected DiskVmElement getOldDiskVmElement() {
        if (oldDiskVmElement == null) {
            oldDiskVmElement = diskVmElementDao.get(new VmDeviceId(getOldDisk().getId(), getVmId()));
        }
        return oldDiskVmElement;
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
            List<Pair<VM, VmDevice>> attachedVmsInfo = vmDao.getVmsWithPlugInfo(getOldDisk().getId());
            for (Pair<VM, VmDevice> pair : attachedVmsInfo) {
                VM vm = pair.getFirst();
                vmsDiskOrSnapshotAttachedTo.add(vm);
                if (Boolean.TRUE.equals(pair.getSecond().isPlugged())) {
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

         TransactionSupport.executeInNewTransaction(() -> {
             getCompensationContext().snapshotEntityStatus(diskImage.getImage());
             getCompensationContext().stateChanged();
             diskImage.setImageStatus(ImageStatus.LOCKED);
             ImagesHandler.updateImageStatus(diskImage.getImageId(), ImageStatus.LOCKED);
             return null;
         });
    }

    public void unlockImageInDb() {
        final DiskImage diskImage = (DiskImage) getOldDisk();
        diskImage.setImageStatus(ImageStatus.OK);
        ImagesHandler.updateImageStatus(diskImage.getImageId(), ImageStatus.OK);
    }

    private AmendImageGroupVolumesCommandParameters amendImageGroupVolumesCommandParameters() {
        DiskImage diskImage = (DiskImage) getNewDisk();
        return new AmendImageGroupVolumesCommandParameters(diskImage.getId(), diskImage.getQcowCompat());
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
        getReturnValue().getValidationMessages().clear();
        getReturnValue().getValidationMessages().addAll(internalReturnValue.getValidationMessages());
        getReturnValue().setValid(internalReturnValue.isValid());
    }
}
