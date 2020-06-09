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

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VmSlaPolicyUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.profiles.DiskProfileHelper;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.storage.disk.image.MetadataDiskDescriptionHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.LocalizedVmStatus;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskVmElementValidator;
import org.ovirt.engine.core.bll.validator.storage.ManagedBlockStorageDomainValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.ActionUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AmendImageGroupVolumesCommandParameters;
import org.ovirt.engine.core.common.action.ExtendImageSizeParameters;
import org.ovirt.engine.core.common.action.ExtendManagedBlockStorageDiskSizeParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.UpdateDiskParameters;
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
import org.ovirt.engine.core.common.businessentities.storage.DiskBackup;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.businessentities.storage.QcowCompat;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.common.vdscommands.SetVolumeDescriptionVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
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
public class UpdateDiskCommand<T extends UpdateDiskParameters> extends AbstractDiskVmCommand<T>
        implements QuotaStorageDependent, SerialChildExecutingCommand {

    /* Multiplier used to convert GB to bytes or vice versa. */
    private static final long BYTES_IN_GB = 1024 * 1024 * 1024;

    private List<PermissionSubject> listPermissionSubjects;
    private final List<VM> vmsDiskSnapshotPluggedTo = new LinkedList<>();
    private final List<VM> vmsDiskPluggedTo = new LinkedList<>();
    private final List<VM> vmsDiskOrSnapshotPluggedTo = new LinkedList<>();
    private final List<VM> vmsDiskOrSnapshotAttachedTo = new LinkedList<>();
    private List<DiskImage> allDiskImages = null;

    @Inject
    private AuditLogDirector auditLogDirector;

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
    @Inject
    private MetadataDiskDescriptionHandler metadataDiskDescriptionHandler;
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;

    public UpdateDiskCommand(T parameters, CommandContext commandContext) {
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
    public UpdateDiskCommand(Guid commandId) {
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

        exclusiveLock.put(getOldDisk().getId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK, EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED));
        return exclusiveLock;
    }

    @Override
    protected void executeVmCommand() {
        // Locking an image in DB is required for extending disks size,
        // which doesn't apply for LUN disks.
        if (getOldDisk().getDiskStorageType().isInternal()) {
            lockImageInDb();
        }
        List<UpdateDiskParameters.Phase> phaseList = new ArrayList<>();
        ImagesHandler.setDiskAlias(getParameters().getDiskInfo(), getVm());
        if (resizeDiskImageRequested()) {
            phaseList.add(UpdateDiskParameters.Phase.EXTEND_DISK);
        }
        if (getOldDisk().getDiskStorageType() == DiskStorageType.IMAGE && amendDiskRequested()) {
            phaseList.add(UpdateDiskParameters.Phase.AMEND_DISK);
        }
        phaseList.add(UpdateDiskParameters.Phase.UPDATE_DISK);
        getParameters().setDiskUpdatePhases(phaseList);
        persistCommandIfNeeded();
        setSucceeded(true);
    }

    @Override
    protected boolean validate() {
        DiskValidator oldDiskValidator = getDiskValidator(getOldDisk());
        if (getVm() != null && !validateVmDisk(oldDiskValidator)) {
            return false;
        }

        if (isAtLeastOneVmIsNotDown(vmsDiskOrSnapshotPluggedTo) && updateDiskParametersRequiringVmDownRequested()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
        }

        boolean isDiskImageOrCinder = DiskStorageType.IMAGE == getOldDisk().getDiskStorageType() ||
                DiskStorageType.CINDER == getOldDisk().getDiskStorageType();

        if (isDiskImageOrCinder) {
            ValidationResult imagesNotLocked = new DiskImagesValidator((DiskImage) getOldDisk()).diskImagesNotLocked();
            if (!imagesNotLocked.isValid()) {
                return validate(imagesNotLocked);
            }
        }

        ValidationResult isHostedEngineDisk = oldDiskValidator.validateNotHostedEngineDisk();
        if (!isHostedEngineDisk.isValid()) {
            return validate(isHostedEngineDisk);
        }

        if (!checkOperationAllowedOnDiskContentType(getOldDisk())) {
            return false;
        }

        // Can't edit diskVmElements attributes for floating disks
        if (getVm() == null && getDiskVmElement() != null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (isDiskImageOrCinder && !validateCanResizeDisk()) {
            return false;
        }

        if (amendDiskRequested() && !validateCanAmendDisk()) {
            return false;
        }
        if (isQcowCompatChangedOnRawDisk()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CANT_AMEND_RAW_DISK);
        }

        if (isIncrementalBackupChangedOnRawDisk()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_INCREMENTAL_BACKUP_NOT_SUPPORTED_FOR_RAW_DISK);
        }

        return validateCanUpdateShareable() && validateQuota() && setAndValidateDiskProfiles();
    }

    private boolean validateVmDisk(DiskValidator oldDiskValidator) {
        if (!validate(new VmValidator(getVm()).isVmExists()) || !isDiskExistAndAttachedToVm(getOldDisk()) ||
                !validateDiskVmData()) {
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
        DiskVmElementValidator diskVmElementValidator = getDiskVmElementValidator(getNewDisk(), getDiskVmElement());
        return validateCanUpdateReadOnly() &&
                validateVmPoolProperties() &&
                validate(diskVmElementValidator.isVirtIoScsiValid(getVm())) &&
                (!isDiskInterfaceUpdated || validate(diskVmElementValidator.isDiskInterfaceSupported(getVm()))) &&
                validatePassDiscardSupported(diskVmElementValidator);
    }

    private boolean isQcowCompatChangedOnRawDisk() {
        if (getNewDisk().getDiskStorageType() == DiskStorageType.IMAGE) {
            QcowCompat qcowCompat = ((DiskImage) getNewDisk()).getQcowCompat();
            if (qcowCompat != QcowCompat.Undefined) {
                List<DiskImage> images = getDiskImages(getOldDisk().getId());
                return images.stream().noneMatch(DiskImage::isQcowFormat);
            }
        }
        return false;
    }

    private boolean isIncrementalBackupChangedOnRawDisk() {
        if (getNewDisk().getDiskStorageType() == DiskStorageType.IMAGE) {
            DiskImage diskImage = (DiskImage) getNewDisk();
            return diskImage.getBackup() == DiskBackup.Incremental &&  diskImage.getVolumeFormat() == VolumeFormat.RAW;
        }
        return false;
    }

    private boolean validatePassDiscardSupported(DiskVmElementValidator diskVmElementValidator) {
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

            List<DiskImage> diskImageList = getDiskImages(getOldDisk().getId());

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
            if (getVmDeviceForVm() != null && Boolean.TRUE.equals(getVmDeviceForVm().getReadOnly())) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_CANNOT_RESIZE_READ_ONLY_DISK);
            }

            if (getVmDeviceForVm() != null && getVmDeviceForVm().getSnapshotId() != null) {
                DiskImage snapshotDisk = diskImageDao.getDiskSnapshotForVmSnapshot(getParameters().getDiskInfo().getId(), vmDeviceForVm.getSnapshotId());
                if (snapshotDisk.getSize() != newDiskImage.getSize()) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_CANNOT_RESIZE_DISK_SNAPSHOT);
                }
            }

            if (oldDiskImage.getSize() > newDiskImage.getSize()) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_REQUESTED_DISK_SIZE_IS_TOO_SMALL);
            }

            StorageDomain storageDomain = storageDomainDao.get(newDiskImage.getStorageIds().get(0));
            if (storageDomain.getStorageType().isBlockDomain()) {
                Integer maxBlockDiskSize = Config.<Integer> getValue(ConfigValues.MaxBlockDiskSizeInGibiBytes);
                if (newDiskImage.getSize() / BYTES_IN_GB > maxBlockDiskSize) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_DISK_MAX_SIZE_EXCEEDED,
                            String.format("$max_disk_size %1$s", maxBlockDiskSize));
                }
            }

            for (VM vm : getVmsDiskPluggedTo()) {
                if (!ActionUtils.canExecute(Collections.singletonList(vm), VM.class, ActionType.ExtendImageSize)) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL, LocalizedVmStatus.from(vm.getStatus()));
                }
            }
            StorageDomainValidator storageDomainValidator = getStorageDomainValidator((DiskImage) getNewDisk());
            if (!validate(storageDomainValidator.isDomainExistAndActive())) {
                return false;
            }

            if (storageDomain.getStorageType().isManagedBlockStorage()) {
                if (!validate(ManagedBlockStorageDomainValidator.isOperationSupportedByManagedBlockStorage(getActionType()))) {
                    return false;
                }
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

    private boolean validateCanAmendDisk() {
        DiskImage disk = (DiskImage) getNewDisk();
        setStoragePoolId(disk.getStoragePoolId());
        if (!validate(new StoragePoolValidator(getStoragePool()).existsAndUp())) {
            return false;
        }
        DiskImagesValidator diskImagesValidator = new DiskImagesValidator(List.of(disk));
        return validate(diskImagesValidator.diskImagesNotIllegal());
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

    protected void performDiskUpdate() {
        if (shouldPerformMetadataUpdate()) {
            updateMetaDataDescription((DiskImage) getNewDisk());
        }
        final Disk diskForUpdate = diskDao.get(getParameters().getDiskInfo().getId());
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
            @Override
            public Object runInTransaction() {
                DiskVmElement diskVmElementForUpdate;
                if (getVm() != null) {
                    vmStaticDao.incrementDbGeneration(getVmId());
                    updateDeviceProperties();
                    diskVmElementForUpdate = diskVmElementDao.get(new VmDeviceId(getOldDisk().getId(), getVmId()));
                    applyUserChanges(diskForUpdate, diskVmElementForUpdate);
                    diskVmElementDao.update(diskVmElementForUpdate);
                } else {
                    applyUserChanges(diskForUpdate, null);
                }
                baseDiskDao.update(diskForUpdate);
                switch (diskForUpdate.getDiskStorageType()) {
                    case IMAGE:
                    case MANAGED_BLOCK_STORAGE:
                        DiskImage diskImage = (DiskImage) diskForUpdate;
                        diskImage.setQuotaId(getQuotaId());
                        imageDao.update(diskImage.getImage());
                        updateQuota(diskImage);
                        updateDiskProfile();
                        break;
                    case CINDER:
                        CinderDisk cinderDisk = (CinderDisk) diskForUpdate;
                        cinderDisk.setQuotaId(getQuotaId());
                        setStorageDomainId(cinderDisk.getStorageIds().get(0));
                        getCinderBroker().updateDisk(cinderDisk);
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
                storageDomainDao.getForStoragePool(diskImage.getStorageIds().get(0), getStoragePoolId());
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
                    new SetVolumeDescriptionVDSCommandParameters(getStoragePoolId(),
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
        return metadataDiskDescriptionHandler.generateJsonDiskDescription(getParameters().getDiskInfo());
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
                vmSlaPolicyUtils.refreshRunningVmsWithDiskImage(newDisk);
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
        diskToUpdate.setBackup(getNewDisk().getBackup());

        if (dveToUpdate != null) {
            dveToUpdate.setBoot(getDiskVmElement().isBoot());
            dveToUpdate.setDiskInterface(getDiskVmElement().getDiskInterface());
            dveToUpdate.setPassDiscard(getDiskVmElement().isPassDiscard());
            dveToUpdate.setUsingScsiReservation(getDiskVmElement().isUsingScsiReservation());
        }
    }

    protected void reloadDisks() {
        if (getVm() != null) {
            vmHandler.updateDisksFromDb(getVm());
        }
    }

    private void extendDiskImageSize() {
        runInternalAction(ActionType.ExtendImageSize, createExtendParameters(),
                createStepsContext(StepEnum.EXTEND_IMAGE));
    }

    private void executeDiskExtend() {
        switch (getOldDisk().getDiskStorageType()) {
            case IMAGE:
                extendDiskImageSize();
                break;
            case CINDER:
                extendCinderDiskSize();
                break;
            case MANAGED_BLOCK_STORAGE:
                extendManagedBlockDiskSize();
                break;
        }
    }

    protected void amendDiskImage() {
        runInternalActionWithTasksContext(ActionType.AmendImageGroupVolumes, createAmendParameters());
    }

    private void extendCinderDiskSize() {
        CinderDisk newCinderDisk = (CinderDisk) getNewDisk();
        Future<ActionReturnValue> future = commandCoordinatorUtil.executeAsyncCommand(
                ActionType.ExtendCinderDisk,
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

    private void extendManagedBlockDiskSize() {
        ManagedBlockStorageDisk newManagedBlockDisk = (ManagedBlockStorageDisk) getNewDisk();
        Future<ActionReturnValue> future = commandCoordinatorUtil.executeAsyncCommand(
                ActionType.ExtendManagedBlockStorageDiskSize,
                buildExtendManagedBlockDiskParameters(newManagedBlockDisk),
                cloneContextAndDetachFromParent());
        try {
            setReturnValue(future.get());
            setSucceeded(getReturnValue().getSucceeded());
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error extending managed block disk '{}': {}",
                    getNewDisk().getDiskAlias(),
                    e.getMessage());
            log.debug("Exception", e);
        }
    }

    private ActionParametersBase buildExtendManagedBlockDiskParameters(ManagedBlockStorageDisk newManagedBlockDisk) {
        ExtendManagedBlockStorageDiskSizeParameters parameters = new ExtendManagedBlockStorageDiskSizeParameters(
                newManagedBlockDisk);
        parameters.setStorageDomainId(newManagedBlockDisk.getStorageIds().get(0));
        parameters.setParametersCurrentUser(getParameters().getParametersCurrentUser());
        parameters.setEndProcedure(EndProcedure.PARENT_MANAGED);
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        return parameters;
    }

    private ActionParametersBase buildExtendCinderDiskParameters(CinderDisk newCinderDisk) {
        VmDiskOperationParameterBase parameters = new VmDiskOperationParameterBase(newCinderDisk);
        parameters.setParametersCurrentUser(getParameters().getParametersCurrentUser());
        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        return parameters;
    }

    @Override
    protected void endSuccessfully() {
        endOperation();
    }

    @Override
    protected void endWithFailure() {
        endOperation();
    }

    private void endOperation() {
        if (getOldDisk().getDiskStorageType().isInternal()) {
            unlockImageInDb();
        }
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            if (isCinderDisk() && resizeDiskImageRequested()) {
                return AuditLogType.USER_EXTENDED_DISK_SIZE;
            } else {
                return getVm() != null ?
                        AuditLogType.USER_UPDATE_VM_DISK : AuditLogType.USER_UPDATE_DISK;
            }
        }
        return getVm() != null ?
                AuditLogType.USER_FAILED_UPDATE_VM_DISK : AuditLogType.USER_FAILED_UPDATE_DISK;
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

    private CommandContext createStepsContext(StepEnum step) {
        Step addedStep = executionHandler.addSubStep(getExecutionContext(),
                getExecutionContext().getJob().getStep(StepEnum.EXECUTING),
                step,
                ExecutionMessageDirector.resolveStepMessage(step, Collections.emptyMap()));
        ExecutionContext ctx = new ExecutionContext();
        ctx.setStep(addedStep);
        ctx.setMonitored(true);
        return ExecutionHandler.createDefaultContextForTasks(getContext(), null)
                .withExecutionContext(ctx);
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

    private boolean isManagedBlockDisk() {
        return isDiskStorageType(DiskStorageType.MANAGED_BLOCK_STORAGE);
    }

    private boolean isDiskStorageType(DiskStorageType diskStorageType) {
        return getOldDisk() != null && getNewDisk() != null && diskStorageType == getOldDisk().getDiskStorageType();
    }

    protected Guid getQuotaId() {
        if (getNewDisk() != null && isInternalManagedDisk()) {
            Guid quotaId = ((DiskImage) getNewDisk()).getQuotaId();
            return getQuotaManager().getFirstQuotaForUser(
                    quotaId,
                    getStoragePoolId(),
                    getCurrentUser());
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
                        QuotaStorageConsumptionParameter.QuotaAction.RELEASE,
                        //TODO: Shared Disk?
                        oldDiskImage.getStorageIds().get(0),
                        (double)oldDiskImage.getSizeInGigabytes()));
            }
        }
        return list;
    }

    protected boolean isInternalManagedDisk() {
        return isDiskImage() || isCinderDisk() || isManagedBlockDisk();
    }

    private QuotaConsumptionParameter generateQuotaConsumeParameters(DiskImage newDiskImage, long sizeInGigabytes) {
        return new QuotaStorageConsumptionParameter(
               newDiskImage.getQuotaId(),
               QuotaStorageConsumptionParameter.QuotaAction.CONSUME,
               //TODO: Shared Disk?
               newDiskImage.getStorageIds().get(0),
               (double) sizeInGigabytes );
    }

    private boolean resizeDiskImageRequested() {
        boolean sizeChanged = getNewDisk().getSize() != getOldDisk().getSize();
        switch (getNewDisk().getDiskStorageType()) {
        case IMAGE:
            return sizeChanged && (getVmDeviceForVm() == null || getVmDeviceForVm().getSnapshotId() == null);
        case MANAGED_BLOCK_STORAGE:
        case CINDER:
            return sizeChanged;
        }
        return false;
    }

    protected boolean amendDiskRequested() {
        // if the updated disk is the base snapshot, no amend operation needed;
        if (getOldDisk().getDiskStorageType() == DiskStorageType.IMAGE && !isBaseSnapshotDisk()) {
            QcowCompat qcowCompat = ((DiskImage) getNewDisk()).getQcowCompat();
            return getDiskImages(getOldDisk().getId()).stream().anyMatch(disk -> disk.isQcowFormat()
                    && disk.getQcowCompat() != qcowCompat);
        }
        return false;
    }

    private boolean isBaseSnapshotDisk() {
        return getNewDisk().isDiskSnapshot()
                && ((DiskImage) getNewDisk()).getImage().getVolumeFormat() != VolumeFormat.COW;
    }

    private List<DiskImage> getDiskImages(Guid diskId) {
        if (allDiskImages == null) {
            allDiskImages = diskImageDao.getAllSnapshotsForImageGroup(diskId);
        }
        return allDiskImages;
    }

    private boolean updateParametersRequiringVmDownRequested() {
        return updateDiskParametersRequiringVmDownRequested() || updateImageParametersRequiringVmDownRequested() ||
                updateDiskVmElementParametersRequiringVmDownRequested();
    }

    private boolean updateDiskVmElementParametersRequiringVmDownRequested() {
        return getOldDiskVmElement().isBoot() != getDiskVmElement().isBoot() ||
                getOldDiskVmElement().getDiskInterface() != getDiskVmElement().getDiskInterface();
    }

    private boolean updateDiskParametersRequiringVmDownRequested() {
        return getOldDisk().getPropagateErrors() != getNewDisk().getPropagateErrors() ||
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
            if (getVm() != null) {
                DiskImage oldDiskImage = (DiskImage) oldDisk;
                Guid vmSnapshotId = isUpdatedToShareable(oldDisk, newDisk) ? null :
                        snapshotDao.getId(getVmId(), SnapshotType.ACTIVE);
                oldDiskImage.setVmSnapshotId(vmSnapshotId);
            }
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

    public void lockImageInDb() {
        final DiskImage diskImage = (DiskImage) getOldDisk();

        TransactionSupport.executeInNewTransaction(() -> {
            getCompensationContext().snapshotEntityStatus(diskImage.getImage());
            getCompensationContext().stateChanged();
            diskImage.setImageStatus(ImageStatus.LOCKED);
            imagesHandler.updateImageStatus(diskImage.getImageId(), ImageStatus.LOCKED);
            return null;
        });
    }

    public void unlockImageInDb() {
        final DiskImage diskImage = (DiskImage) getOldDisk();
        diskImage.setImageStatus(ImageStatus.OK);
        imagesHandler.updateImageStatus(diskImage.getImageId(), ImageStatus.OK);
    }

    private AmendImageGroupVolumesCommandParameters amendImageGroupVolumesCommandParameters() {
        DiskImage diskImage = (DiskImage) getNewDisk();
        return new AmendImageGroupVolumesCommandParameters(diskImage.getId(), diskImage.getQcowCompat());
    }

    @Override
    public Guid getStoragePoolId() {
        if (getVm() != null) {
            return super.getStoragePoolId();
        } else if (isInternalManagedDisk()) {
            return ((DiskImage) getNewDisk()).getStoragePoolId();
        }
        return null;
    }

    private AmendImageGroupVolumesCommandParameters createAmendParameters() {
        DiskImage diskImage = (DiskImage) getNewDisk();
        AmendImageGroupVolumesCommandParameters parameters =
                new AmendImageGroupVolumesCommandParameters(diskImage.getId(), diskImage.getQcowCompat());
        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        return parameters;
    }

    private ExtendImageSizeParameters createExtendParameters() {
        DiskImage diskImage = (DiskImage) getNewDisk();
        ExtendImageSizeParameters params = new ExtendImageSizeParameters(diskImage.getImageId(), diskImage.getSize());
        params.setStoragePoolId(diskImage.getStoragePoolId());
        params.setStorageDomainId(diskImage.getStorageIds().get(0));
        params.setImageGroupID(diskImage.getId());
        params.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        params.setParentCommand(getActionType());
        params.setParentParameters(getParameters());
        return params;
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        if (completedChildCount == getParameters().getDiskUpdatePhases().size()) {
            return false;
        }
        UpdateDiskParameters.Phase nextPhase = getParameters().getDiskUpdatePhases().get(completedChildCount);
        switch (nextPhase) {
            case EXTEND_DISK:
                log.info("Starting to extend disk id '{}')", getOldDisk().getId());
                executeDiskExtend();
                break;
            case AMEND_DISK:
                log.info("Starting to amend disk id '{}')", getOldDisk().getId());
                amendDiskImage();
                break;
            case UPDATE_DISK:
                log.info("Starting to update general fields of disk id '{}')", getOldDisk().getId());
                performDiskUpdate();
                // If the only phase is update then return false.
                return false;
        }
        return true;
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }
}
