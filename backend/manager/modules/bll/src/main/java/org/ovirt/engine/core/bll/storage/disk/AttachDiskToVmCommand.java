package org.ovirt.engine.core.bll.storage.disk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AttachDetachVmDiskParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;

public class AttachDiskToVmCommand<T extends AttachDetachVmDiskParameters> extends AbstractDiskVmCommand<T> {

    private List<PermissionSubject> permsList = null;
    private Disk disk;

    public AttachDiskToVmCommand(T parameters) {
        this(parameters, null);
    }

    public AttachDiskToVmCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        disk = loadDisk(getParameters().getEntityInfo().getId());
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected boolean validate() {
        if (disk == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_IMAGE_DOES_NOT_EXIST);
        }

        DiskValidator oldDiskValidator = new DiskValidator(disk);
        ValidationResult isHostedEngineDisk = oldDiskValidator.validateNotHostedEngineDisk();
        if (!isHostedEngineDisk.isValid()) {
            return validate(isHostedEngineDisk);
        }

        disk.setReadOnly(getParameters().isReadOnly());
        DiskValidator diskValidator = getDiskValidator(disk);

        if (!checkDiskUsedAsOvfStore(diskValidator)) {
            return false;
        }

        if (isOperationPerformedOnDiskSnapshot()
                && (!validate(getSnapshotsValidator().snapshotExists(getSnapshot())) || !validate(getSnapshotsValidator().snapshotTypeSupported(getSnapshot(),
                Collections.singletonList(SnapshotType.REGULAR))))) {
            return false;
        }

        boolean isImageDisk = disk.getDiskStorageType().isInternal();

        if (isImageDisk) {
            //TODO : this load and check of the active disk will be removed
            //after inspecting upgrade
            Disk activeDisk = loadActiveDisk(disk.getId());

            if (((DiskImage) activeDisk).getImageStatus() == ImageStatus.ILLEGAL) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_ILLEGAL_DISK_OPERATION);
            }

            if (((DiskImage) disk).getImageStatus() == ImageStatus.LOCKED) {
                addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED);
                addValidationMessageVariable("diskAliases", disk.getDiskAlias());
                return false;
            }
        }

        if (!isVmExist() || !isVmInUpPausedDownStatus()) {
            return false;
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        updateDisksFromDb();
        if (!isDiskCanBeAddedToVm(disk, getVm()) || !isDiskPassPciAndIdeLimit(disk)) {
            return false;
        }

        if (getVmDeviceDao().exists(new VmDeviceId(disk.getId(), getVmId()))) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DISK_ALREADY_ATTACHED);
        }

        if (disk.isShareable()
                && !isVersionSupportedForShareable(disk, getStoragePoolDao().get(getVm().getStoragePoolId())
                        .getCompatibilityVersion()
                        .getValue())) {
            return failValidation(EngineMessage.ACTION_NOT_SUPPORTED_FOR_CLUSTER_POOL_LEVEL);
        }

        if (!isOperationPerformedOnDiskSnapshot() && !disk.isShareable() && disk.getNumberOfVms() > 0) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_NOT_SHAREABLE_DISK_ALREADY_ATTACHED);
        }

        if (isImageDisk && getStoragePoolIsoMapDao().get(new StoragePoolIsoMapId(
                ((DiskImage) disk).getStorageIds().get(0), getVm().getStoragePoolId())) == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_MATCH);
        }
        if (isImageDisk) {
            StorageDomain storageDomain = getStorageDomainDao().getForStoragePool(
                    ((DiskImage) disk).getStorageIds().get(0), ((DiskImage) disk).getStoragePoolId());
            StorageDomainValidator storageDomainValidator = getStorageDomainValidator(storageDomain);
            if (!validate(storageDomainValidator.isDomainExistAndActive())) {
                return false;
            }
        }

        if (!validate(diskValidator.isReadOnlyPropertyCompatibleWithInterface())) {
            return false;
        }

        if (!validate(diskValidator.isVirtIoScsiValid(getVm()))) {
            return false;
        }

        if (!validate(diskValidator.isDiskInterfaceSupported(getVm()))) {
            return false;
        }

        if (!isVmNotInPreviewSnapshot()) {
            return false;
        }

        if (getParameters().isPlugUnPlug()
                && getVm().getStatus() != VMStatus.Down) {
            return canPerformDiskHotPlug(disk);
        }
        return true;
    }

    protected StorageDomainValidator getStorageDomainValidator(StorageDomain storageDomain) {
        return new StorageDomainValidator(storageDomain);
    }

    @Override
    protected void executeVmCommand() {
        if (!isOperationPerformedOnDiskSnapshot()) {
            getVmStaticDao().incrementDbGeneration(getVm().getId());
        }

        final VmDevice vmDevice = createVmDevice();
        getVmDeviceDao().save(vmDevice);

        // update cached image
        List<Disk> imageList = new ArrayList<>();
        imageList.add(disk);
        VmHandler.updateDisksForVm(getVm(), imageList);

        if (!isOperationPerformedOnDiskSnapshot()) {
            if (disk.isAllowSnapshot()) {
                updateDiskVmSnapshotId();
            }
        }

        // update vm device boot order
        updateBootOrderInVmDevice();

        if (getParameters().isPlugUnPlug() && getVm().getStatus() != VMStatus.Down) {
            performPlugCommand(VDSCommandType.HotPlugDisk, disk, vmDevice);
        }
        setSucceeded(true);
    }

    protected VmDevice createVmDevice() {
        return new VmDevice(new VmDeviceId(disk.getId(), getVmId()),
                VmDeviceGeneralType.DISK,
                VmDeviceType.DISK.getName(),
                "",
                0,
                null,
                true,
                getParameters().isPlugUnPlug(),
                getParameters().isReadOnly(),
                "",
                null,
                getParameters().getSnapshotId(), null);
    }

    protected boolean isOperationPerformedOnDiskSnapshot() {
        return getParameters().getSnapshotId() != null;
    }

    protected void updateBootOrderInVmDevice() {
        VmDeviceUtils.updateBootOrder(getVm().getId());
    }

    private void updateDiskVmSnapshotId() {
        Guid snapshotId = getSnapshotDao().getId(getVmId(), SnapshotType.ACTIVE);
        if (disk.getDiskStorageType().isInternal()) {
            DiskImage diskImage = (DiskImage) disk;
            getImageDao().updateImageVmSnapshotId(diskImage.getImageId(),
                    snapshotId);
        } else {
            throw new EngineException(EngineError.StorageException,
                    "update of snapshot id was initiated for unsupported disk type");
        }
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__ATTACH_ACTION_TO);
        addValidationMessage(EngineMessage.VAR__TYPE__VM_DISK);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        if (permsList == null) {
            permsList = super.getPermissionCheckSubjects();
            Guid diskId = disk == null ? null : disk.getId();
            permsList.add(new PermissionSubject(diskId, VdcObjectType.Disk, ActionGroup.ATTACH_DISK));
        }
        return permsList;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (disk == null) {
            return null;
        }

        Map<String, Pair<String, String>> locks = new HashMap<>();
        if (!disk.isShareable()) {
            locks.put(disk.getId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        }

        if (disk.isBoot()) {
            locks.put(getParameters().getVmId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_DISK_BOOT, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        }

        return locks;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ATTACH_DISK_TO_VM : AuditLogType.USER_FAILED_ATTACH_DISK_TO_VM;
    }


    protected Snapshot getSnapshot() {
        return getSnapshotDao().get(getParameters().getSnapshotId());
    }

    @Override
    public String getDiskAlias() {
        return disk.getDiskAlias();
    }

}
