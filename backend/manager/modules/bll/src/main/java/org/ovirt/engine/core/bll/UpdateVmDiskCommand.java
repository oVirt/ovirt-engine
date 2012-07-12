package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class UpdateVmDiskCommand<T extends UpdateVmDiskParameters> extends AbstractDiskVmCommand<T> {

    private static final long serialVersionUID = 5915267156998835363L;
    private List<PermissionSubject> listPermissionSubjects;
    private final Disk _oldDisk;
    private boolean shouldUpdateQuotaForDisk;
    private Map<String, String> sharedLockMap;
    private Map<String, String> exclusiveLockMap;

    public UpdateVmDiskCommand(T parameters) {
        super(parameters);
        _oldDisk = getDiskDao().get(getParameters().getDiskId());
    }

    @Override
    protected void ExecuteVmCommand() {
        perforDiskUpdate();
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = isVmExist();
        if (retValue) {
            if (!isDiskExist(_oldDisk)) {
                return false;
            }

            List<VM> listVms = getVmDAO().getForDisk(_oldDisk.getId()).get(Boolean.TRUE);
            buidSharedLockMap(listVms);
            buidExclusiveLockMap(listVms);
            acquireLockInternal();

            // Check if all VMs are in status down.
            if (listVms != null && !listVms.isEmpty()) {
                for (VM vm : listVms) {
                    if (vm.getstatus() != VMStatus.Down) {
                        addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
                        return false;
                    }
                }
            }
            retValue = checkCanPerformRegularUpdate();
        }
        return retValue;
    }

    private void buidSharedLockMap(List<VM> listVms) {
        if (listVms != null && !listVms.isEmpty()) {
            sharedLockMap = new HashMap<String, String>();
            for (VM vm : listVms) {
                sharedLockMap.put(vm.getId().toString(), LockingGroup.VM.name());
            }
        }
    }

    private void buidExclusiveLockMap(List<VM> listVms) {
        if (getParameters().getDiskInfo().isBoot() && listVms != null && !listVms.isEmpty()) {
            exclusiveLockMap = new HashMap<String, String>();
            for (VM vm : listVms) {
                exclusiveLockMap.put(vm.getId().toString(), LockingGroup.VM_DISK_BOOT.name());
            }
        }
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_DISK);
    }

    @Override
    protected boolean validateQuota() {
        boolean quotaValid = true;
        if (DiskStorageType.IMAGE == _oldDisk.getDiskStorageType()) {
            shouldUpdateQuotaForDisk = !((DiskImage)_oldDisk).getQuotaId().equals(getQuotaId());
            if (shouldUpdateQuotaForDisk) {
                // Set default quota id if storage pool enforcement is disabled.
                getParameters().setQuotaId(QuotaHelper.getInstance().getQuotaIdToConsume(getQuotaId(),
                        getStoragePool()));
                setStorageDomainId(((DiskImage)_oldDisk).getstorage_ids().get(0).getValue());
                quotaValid = (QuotaManager.validateStorageQuota(getStorageDomainId().getValue(),
                        getParameters().getQuotaId(),
                        getStoragePool().getQuotaEnforcementType(),
                        new Double(((DiskImage) getParameters().getDiskInfo()).getSizeInGigabytes()),
                        getCommandId(),
                        getReturnValue().getCanDoActionMessages()));
            }
        }
        return quotaValid;
    }

    private boolean checkCanPerformRegularUpdate() {
        boolean retValue = true;
        if (_oldDisk.getDiskInterface() != getParameters().getDiskInfo().getDiskInterface()) {
            List<VmNetworkInterface> allVmInterfaces = DbFacade.getInstance()
                    .getVmNetworkInterfaceDAO().getAllForVm(getVmId());

            List<Disk> allVmDisks = getDiskDao().getAllForVm(getVmId());
            allVmDisks.removeAll(LinqUtils.filter(allVmDisks, new Predicate<Disk>() {
                @Override
                public boolean eval(Disk o) {
                    return o.getId().equals(
                            _oldDisk.getId());
                }
            }));
            allVmDisks.add(getParameters().getDiskInfo());
            if (!CheckPCIAndIDELimit(getVm().getnum_of_monitors(),
                    allVmInterfaces,
                    allVmDisks,
                    getReturnValue().getCanDoActionMessages())) {
                retValue = false;
            }
        }

        // Validate update boot disk.
        if (retValue && getParameters().getDiskInfo().isBoot()) {
            VmHandler.updateDisksFromDb(getVm());
            for (Disk disk : getVm().getDiskMap().values()) {
                if (disk.isBoot() && !disk.getId().equals(_oldDisk.getId())) {
                    retValue = false;
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_BOOT_IN_USE);
                    getReturnValue().getCanDoActionMessages().add(
                            String.format("$DiskName %1$s", disk.getDiskAlias()));
                    break;
                }
            }

            // If disk is shareable and it is also bootable return an appropriate CDA message.
            if (getParameters().getDiskInfo().isBoot() && getParameters().getDiskInfo().isShareable()) {
                addCanDoActionMessage(VdcBllMessages.SHAREABLE_DISK_IS_NOT_SUPPORTED_FOR_DISK);
                return false;
            }
        }

        // Set disk alias name in the disk retrieved from the parameters.
        ImagesHandler.setDiskAlias(getParameters().getDiskInfo(), getVm());
        return retValue && validateShareableDisk();
    }

    /**
     * Validate whether a disk can be shareable. Disk can be shareable if it is not based on qcow FS,
     * which means it should not be based on a template image with thin provisioning,
     * it also should not contain snapshots and it is not bootable.
     * @return Indication whether the disk can be shared or not.
     */
    protected boolean validateShareableDisk() {
        if (DiskStorageType.LUN == _oldDisk.getDiskStorageType()) {
            return true;
        }
        boolean isDiskUpdatedToShareable = getParameters().getDiskInfo().isShareable();
        boolean isDiskShareable = _oldDisk.isShareable();

        // Check if VM is not during snapshot.
        if (ImagesHandler.isVmInPreview(getVmId())) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_IN_PREVIEW);
            return false;
        }

        // If user want to update the disk to be shareable then update the vm snapshot id to be null.
        if (!isDiskShareable && isDiskUpdatedToShareable) {
            List<DiskImage> diskImageList =
                    getDiskImageDao().getAllSnapshotsForImageGroup(_oldDisk.getId());

            // If disk image list is more then one then we assume that it has a snapshot, since one image is the active
            // disk and all the other images are the snapshots.
            if ((diskImageList.size() > 1) || !Guid.Empty.equals(((DiskImage)_oldDisk).getit_guid())) {
                addCanDoActionMessage(VdcBllMessages.SHAREABLE_DISK_IS_NOT_SUPPORTED_FOR_DISK);
                return false;
            }
            if (!isVersionSupportedForShareable(_oldDisk, getStoragePoolDAO().get(getVm().getstorage_pool_id())
                    .getcompatibility_version()
                    .getValue())) {
                addCanDoActionMessage(VdcBllMessages.ACTION_NOT_SUPPORTED_FOR_CLUSTER_POOL_LEVEL);
                return false;
            }

            // Set disk attributes when disk updated to be shareable.
            _oldDisk.setAllowSnapshot(Boolean.FALSE);
            ((DiskImage)_oldDisk).setvm_snapshot_id(null);
        } else if (isDiskShareable && !isDiskUpdatedToShareable) {
            if (getVmDAO().getVmsListForDisk(_oldDisk.getId()).size() > 1) {
                addCanDoActionMessage(VdcBllMessages.DISK_IS_ALREADY_SHARED_BETWEEN_VMS);
                return false;
            }

            // If disk is not floating, then update its vm snapshot id to the active VM snapshot.
            ((DiskImage) _oldDisk).setvm_snapshot_id(DbFacade.getInstance()
                    .getSnapshotDao()
                    .getId(getVmId(), SnapshotType.ACTIVE)
                    .getValue());

            // If disk is shared then the disk should not allow snapshots.
            _oldDisk.setAllowSnapshot(Boolean.TRUE);
        }
        return true;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        if (listPermissionSubjects == null) {
            listPermissionSubjects = new ArrayList<PermissionSubject>();

            Guid diskId = _oldDisk == null ? null : _oldDisk.getId();
            listPermissionSubjects.add(new PermissionSubject(diskId,
                    VdcObjectType.Disk,
                    ActionGroup.EDIT_DISK_PROPERTIES));

            listPermissionSubjects =
                    QuotaHelper.getInstance().addQuotaPermissionSubject(listPermissionSubjects,
                            getStoragePool(),
                            getQuotaId());
        }
        return listPermissionSubjects;
    }

    @Override
    protected void removeQuotaCommandLeftOver() {
        if (shouldUpdateQuotaForDisk) {
            QuotaManager.removeStorageDeltaQuotaCommand(getQuotaId(),
                    getStorageDomainId().getValue(),
                    getStoragePool().getQuotaEnforcementType(),
                    getCommandId());
        }
    }

    private void perforDiskUpdate() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
            @Override
            public Object runInTransaction() {
                clearAddressOnInterfaceChange();
                _oldDisk.setBoot(getParameters().getDiskInfo().isBoot());
                _oldDisk.setDiskInterface(getParameters().getDiskInfo().getDiskInterface());
                _oldDisk.setPropagateErrors(getParameters().getDiskInfo().getPropagateErrors());
                _oldDisk.setWipeAfterDelete(getParameters().getDiskInfo().isWipeAfterDelete());
                _oldDisk.setDiskAlias(getParameters().getDiskInfo().getDiskAlias());
                _oldDisk.setDiskDescription(getParameters().getDiskInfo().getDiskDescription());
                _oldDisk.setShareable(getParameters().getDiskInfo().isShareable());
                DbFacade.getInstance().getBaseDiskDao().update(_oldDisk);
                if (_oldDisk.getDiskStorageType() == DiskStorageType.IMAGE) {
                    DiskImage diskImage = (DiskImage) _oldDisk;
                    diskImage.setQuotaId(getQuotaId());
                    getImageDao().update(diskImage.getImage());
                }
                // update cached image
                VmHandler.updateDisksFromDb(getVm());
                // update vm device boot order
                VmDeviceUtils.updateBootOrderInVmDevice(getVm().getStaticData());

                setSucceeded(UpdateVmInSpm(getVm().getstorage_pool_id(),
                        Arrays.asList(getVm())));
                return null;
            }

            private void clearAddressOnInterfaceChange() {
                // clear the disk address if the type has changed
                if (_oldDisk.getDiskInterface() != getParameters().getDiskInfo().getDiskInterface()) {
                    getVmDeviceDao().clearDeviceAddress(getVmDeviceDao().get(new VmDeviceId(_oldDisk.getId(), getVmId()))
                            .getDeviceId());
                }
            }
        });
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_VM_DISK : AuditLogType.USER_FAILED_UPDATE_VM_DISK;
    }

    @Override
    public String getDiskAlias() {
        return _oldDisk.getDiskAlias();
    }

    @Override
    protected Map<String, String> getSharedLocks() {
        return sharedLockMap;
    }

    @Override
    protected Map<String, String> getExclusiveLocks() {
        return exclusiveLockMap;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put("diskalias", getDiskAlias());
        }
        return jobProperties;
    }

}
