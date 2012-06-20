package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AttachDettachVmDiskParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class AttachDiskToVmCommand<T extends AttachDettachVmDiskParameters> extends AbstractDiskVmCommand<T> {

    private static final long serialVersionUID = -1686587389737849288L;
    private List<PermissionSubject> permsList = null;
    private final Disk disk;

    public AttachDiskToVmCommand(T parameters) {
        super(parameters);
        disk = getDiskDao().get((Guid)getParameters().getEntityId());
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = true;
        if (disk == null) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_IMAGE_DOES_NOT_EXIST);
        }
        retValue =
                retValue && acquireLockInternal() && isVmExist() && isVmUpOrDown() && isDiskCanBeAddedToVm(disk)
                        && isDiskPassPCIAndIDELimit(disk);
        if (retValue && getVmDeviceDao().exists(new VmDeviceId(disk.getId(), getVmId()))) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_ALREADY_ATTACHED);
        }
        if (retValue
                && disk.isShareable()
                && !isVersionSupportedForShareable(disk, getStoragePoolDAO().get(getVm().getstorage_pool_id())
                        .getcompatibility_version()
                        .getValue())) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_NOT_SUPPORTED_FOR_CLUSTER_POOL_LEVEL);
        }
        if (retValue && disk.getDiskStorageType() == DiskStorageType.IMAGE
                && getStoragePoolIsoMapDao().get(new StoragePoolIsoMapId(
                        ((DiskImage) disk).getstorage_ids().get(0), getVm().getstorage_pool_id())) == null) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_MATCH);
        }
        if (retValue && disk.getDiskStorageType() == DiskStorageType.IMAGE) {
            retValue = validate(new SnapshotsValidator().vmNotDuringSnapshot(getVm().getId()));
        }
        if (retValue && getParameters().isPlugUnPlug()
                && getVm().getstatus() != VMStatus.Down) {
            retValue = isOSSupportingHotPlug() && isHotPlugSupported()
                    && isInterfaceSupportedForPlugUnPlug(disk);
        }
        return retValue;
    }

    @Override
    protected void ExecuteVmCommand() {
        final VmDevice vmDevice =
                new VmDevice(new VmDeviceId(disk.getId(), getVmId()),
                        VmDeviceType.DISK.getName(),
                        VmDeviceType.DISK.getName(),
                        "",
                        0,
                        null,
                        true,
                        getParameters().isPlugUnPlug(),
                        false,
                        "");
        getVmDeviceDao().save(vmDevice);
        // update cached image
        List<Disk> imageList = new ArrayList<Disk>();
        imageList.add(disk);
        VmHandler.updateDisksForVm(getVm(), imageList);
        // update vm device boot order
        VmDeviceUtils.updateBootOrderInVmDevice(getVm().getStaticData());
        if (getParameters().isPlugUnPlug() && getVm().getstatus() != VMStatus.Down) {
            performPlugCommnad(VDSCommandType.HotPlugDisk, disk, vmDevice);
        }
        setSucceeded(true);
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
    protected Map<Guid, String> getExclusiveLocks() {
        if (disk.isBoot()) {
            return Collections.singletonMap(getParameters().getVmId(), LockingGroup.VM_DISK_BOOT.name());
        }
        return null;
    }

}
