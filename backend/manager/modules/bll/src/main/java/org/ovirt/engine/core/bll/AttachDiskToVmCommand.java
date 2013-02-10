package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AttachDettachVmDiskParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class AttachDiskToVmCommand<T extends AttachDettachVmDiskParameters> extends AbstractDiskVmCommand<T> {

    private static final long serialVersionUID = -1686587389737849288L;
    private List<PermissionSubject> permsList = null;
    private Disk disk;

    public AttachDiskToVmCommand(T parameters) {
        super(parameters);
        disk = loadDiskById((Guid) getParameters().getEntityId());
    }

    protected AttachDiskToVmCommand(T parameters, Disk disk) {
        super(parameters);
        this.disk = disk;
    }

    private Disk loadDiskById(Guid id) {
        return getDiskDao().get(id);
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = true;
        if (disk == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_IMAGE_DOES_NOT_EXIST);
            return false;
        }
        boolean isImageDisk = disk.getDiskStorageType() == DiskStorageType.IMAGE;
        if (isImageDisk && ((DiskImage) disk).getImageStatus() == ImageStatus.ILLEGAL) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_ILLEGAL_DISK_OPERATION);
            return false;
        }

        retValue = acquireLockInternal();

        if (retValue && isImageDisk && ((DiskImage) disk).getImageStatus() == ImageStatus.LOCKED) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISKS_LOCKED);
            addCanDoActionMessage(String.format("$%1$s %2$s", "diskAliases", disk.getDiskAlias()));
            return false;
        }

        retValue = retValue && isVmExist() && isVmUpOrDown() && isDiskCanBeAddedToVm(disk)
                && isDiskPassPciAndIdeLimit(disk);

        if (retValue && getVmDeviceDao().exists(new VmDeviceId(disk.getId(), getVmId()))) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_ALREADY_ATTACHED);
        }
        if (retValue
                && disk.isShareable()
                && !isVersionSupportedForShareable(disk, getStoragePoolDAO().get(getVm().getStoragePoolId())
                        .getcompatibility_version()
                        .getValue())) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_NOT_SUPPORTED_FOR_CLUSTER_POOL_LEVEL);
        }
        if (retValue && !disk.isShareable() && disk.getNumberOfVms() > 0) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NOT_SHAREABLE_DISK_ALREADY_ATTACHED);
        }
        if (retValue && isImageDisk && getStoragePoolIsoMapDao().get(new StoragePoolIsoMapId(
                ((DiskImage) disk).getstorage_ids().get(0), getVm().getStoragePoolId())) == null) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_MATCH);
        }
        if (retValue && isImageDisk) {
            retValue = validate(new SnapshotsValidator().vmNotDuringSnapshot(getVm().getId()));
        }
        if (retValue && getParameters().isPlugUnPlug()
                && getVm().getStatus() != VMStatus.Down) {
            retValue = isOsSupportingHotPlug() && isHotPlugSupported()
                    && isInterfaceSupportedForPlugUnPlug(disk);
        }
        return retValue;
    }

    @Override
    protected void executeVmCommand() {
        getVmStaticDAO().incrementDbGeneration(getVm().getId());
        final VmDevice vmDevice = createVmDevice();
        getVmDeviceDao().save(vmDevice);
        // update cached image
        List<Disk> imageList = new ArrayList<Disk>();
        imageList.add(disk);
        VmHandler.updateDisksForVm(getVm(), imageList);
        if (disk.isAllowSnapshot()) {
            updateDiskVmSnapshotId();
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
                VmDeviceType.DISK.getName(),
                VmDeviceType.DISK.getName(),
                "",
                0,
                null,
                true,
                getParameters().isPlugUnPlug(),
                false,
                "");
    }

    protected void updateBootOrderInVmDevice() {
        VmDeviceUtils.updateBootOrderInVmDeviceAndStoreToDB(getVm().getStaticData());
    }

    private void updateDiskVmSnapshotId() {
        Guid snapshotId = getSnapshotDao().getId(getVmId(), SnapshotType.ACTIVE);
        if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
            DiskImage diskImage = ((DiskImage) disk);
            getImageDao().updateImageVmSnapshotId(diskImage.getImageId(),
                    snapshotId);
        } else {
            throw new VdcBLLException(VdcBllErrors.StorageException,
                    "update of snapshot id was initiated for unsupported disk type");
        }
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ATTACH_ACTION_TO);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_DISK);
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
    protected Map<String, String> getExclusiveLocks() {
        Map<String, String> locks = new HashMap<String, String>();
        if (!disk.isShareable()) {
            locks.put(disk.getId().toString(), LockingGroup.DISK.name());
        }

        if (disk.isBoot()) {
            locks.put(getParameters().getVmId().toString(), LockingGroup.VM_DISK_BOOT.name());
        }

        return locks;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ATTACH_DISK_TO_VM : AuditLogType.USER_FAILED_ATTACH_DISK_TO_VM;
    }

    @Override
    public String getDiskAlias() {
        return disk.getDiskAlias();
    }
}
