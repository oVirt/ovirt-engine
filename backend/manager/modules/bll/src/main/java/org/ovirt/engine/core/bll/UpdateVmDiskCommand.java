package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class UpdateVmDiskCommand<T extends UpdateVmDiskParameters> extends AbstractDiskVmCommand<T>
        implements QuotaStorageDependent {

    private static final long serialVersionUID = 5915267156998835363L;
    private List<PermissionSubject> listPermissionSubjects;
    private final Disk oldDisk;
    private final Disk newDisk;
    private Map<String, String> sharedLockMap;
    private Map<String, String> exclusiveLockMap;

    public UpdateVmDiskCommand(T parameters) {
        super(parameters);
        oldDisk = getDiskDao().get(getParameters().getDiskId());
        newDisk = getParameters().getDiskInfo();
    }

    @Override
    protected void executeVmCommand() {
        performDiskUpdate();
    }

    @Override
    protected boolean canDoAction() {
        if (!isVmExist()) {
            return false;
        }
        if (!isDiskExist(oldDisk)) {
            return false;
        }

        List<VM> vmsDiskPluggedTo = getVmDAO().getForDisk(oldDisk.getId()).get(Boolean.TRUE);

        if (vmsDiskPluggedTo != null && !vmsDiskPluggedTo.isEmpty()) {
            buildSharedLockMap(vmsDiskPluggedTo);
            buildExclusiveLockMap(vmsDiskPluggedTo);
            acquireLockInternal();

            // Check if all VMs are in status down.
            for (VM vm : vmsDiskPluggedTo) {
                if (vm.getStatus() != VMStatus.Down) {
                    return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
                }
            }
        }

        return checkCanPerformRegularUpdate();
    }


    private void buildSharedLockMap(List<VM> vmsDiskPluggedTo) {
            sharedLockMap = new HashMap<String, String>();
        for (VM vm : vmsDiskPluggedTo) {
                sharedLockMap.put(vm.getId().toString(), LockingGroup.VM.name());
            }
    }

    private void buildExclusiveLockMap(List<VM> vmsDiskPluggedTo) {
        if (newDisk.isBoot()) {
            exclusiveLockMap = new HashMap<String, String>();
            for (VM vm : vmsDiskPluggedTo) {
                exclusiveLockMap.put(vm.getId().toString(), LockingGroup.VM_DISK_BOOT.name());
            }
        }
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_DISK);
    }

    private boolean checkCanPerformRegularUpdate() {
        if (oldDisk.getDiskInterface() != newDisk.getDiskInterface()) {
            List<VmNetworkInterface> allVmInterfaces = DbFacade.getInstance()
                    .getVmNetworkInterfaceDao().getAllForVm(getVmId());

            List<Disk> allVmDisks = getDiskDao().getAllForVm(getVmId());
            allVmDisks.removeAll(LinqUtils.filter(allVmDisks, new Predicate<Disk>() {
                @Override
                public boolean eval(Disk o) {
                    return o.getId().equals(
                            oldDisk.getId());
                }
            }));
            allVmDisks.add(newDisk);
            if (!checkPciAndIdeLimit(getVm().getNumOfMonitors(),
                    allVmInterfaces,
                    allVmDisks,
                    getReturnValue().getCanDoActionMessages())) {
                return false;
            }
        }

        // Validate update boot disk.
        if (newDisk.isBoot()) {
            VmHandler.updateDisksFromDb(getVm());
            for (Disk disk : getVm().getDiskMap().values()) {
                if (disk.isBoot() && !disk.getId().equals(oldDisk.getId())) {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_BOOT_IN_USE);
                    getReturnValue().getCanDoActionMessages().add(
                            String.format("$DiskName %1$s", disk.getDiskAlias()));
                    return false;
                }
            }
        }

        // Set disk alias name in the disk retrieved from the parameters.
        ImagesHandler.setDiskAlias(newDisk, getVm());
        return validateShareableDisk();
    }

    /**
     * Validate whether a disk can be shareable. Disk can be shareable if it is not based on qcow FS,
     * which means it should not be based on a template image with thin provisioning,
     * it also should not contain snapshots and it is not bootable.
     * @return Indication whether the disk can be shared or not.
     */
    private boolean validateShareableDisk() {
        if (DiskStorageType.LUN == oldDisk.getDiskStorageType()) {
            return true;
        }
        boolean isDiskUpdatedToShareable = newDisk.isShareable();
        boolean isOldDiskShareable = oldDisk.isShareable();

        // Check if VM is not during snapshot.
        if (getSnapshotDao().exists(getVmId(), SnapshotStatus.IN_PREVIEW)) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_IN_PREVIEW);
            return false;
        }

        if (!isOldDiskShareable && isDiskUpdatedToShareable) {
            List<DiskImage> diskImageList =
                    getDiskImageDao().getAllSnapshotsForImageGroup(oldDisk.getId());

            // If disk image list is more then one then we assume that it has a snapshot, since one image is the active
            // disk and all the other images are the snapshots.
            if ((diskImageList.size() > 1) || !Guid.Empty.equals(((DiskImage) oldDisk).getit_guid())) {
                addCanDoActionMessage(VdcBllMessages.SHAREABLE_DISK_IS_NOT_SUPPORTED_FOR_DISK);
                return false;
            }
            if (!isVersionSupportedForShareable(oldDisk, getStoragePoolDAO().get(getVm().getStoragePoolId())
                    .getcompatibility_version()
                    .getValue())) {
                addCanDoActionMessage(VdcBllMessages.ACTION_NOT_SUPPORTED_FOR_CLUSTER_POOL_LEVEL);
                return false;
            }

            DiskImage diskImage = (DiskImage) newDisk;
            if (!isVolumeFormatSupportedForShareable(diskImage.getvolume_format())) {
                addCanDoActionMessage(VdcBllMessages.SHAREABLE_DISK_IS_NOT_SUPPORTED_BY_VOLUME_FORMAT);
                return false;
            }

            // If user want to update the disk to be shareable then update the vm snapshot id to be null.
            ((DiskImage) oldDisk).setvm_snapshot_id(null);
        } else if (isOldDiskShareable && !isDiskUpdatedToShareable) {
            if (getVmDAO().getVmsListForDisk(oldDisk.getId()).size() > 1) {
                addCanDoActionMessage(VdcBllMessages.DISK_IS_ALREADY_SHARED_BETWEEN_VMS);
                return false;
            }

            // If disk is not floating, then update its vm snapshot id to the active VM snapshot.
            ((DiskImage) oldDisk).setvm_snapshot_id(DbFacade.getInstance()
                    .getSnapshotDao()
                    .getId(getVmId(), SnapshotType.ACTIVE)
                    .getValue());

        }
        return true;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        if (listPermissionSubjects == null) {
            listPermissionSubjects = new ArrayList<PermissionSubject>();

            Guid diskId = oldDisk == null ? null : oldDisk.getId();
            listPermissionSubjects.add(new PermissionSubject(diskId,
                    VdcObjectType.Disk,
                    ActionGroup.EDIT_DISK_PROPERTIES));
        }
        return listPermissionSubjects;
    }

    private void performDiskUpdate() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
            @Override
            public Object runInTransaction() {
                getVmStaticDAO().incrementDbGeneration(getVm().getId());
                clearAddressOnInterfaceChange();
                oldDisk.setBoot(newDisk.isBoot());
                oldDisk.setDiskInterface(newDisk.getDiskInterface());
                oldDisk.setPropagateErrors(newDisk.getPropagateErrors());
                oldDisk.setWipeAfterDelete(newDisk.isWipeAfterDelete());
                oldDisk.setDiskAlias(newDisk.getDiskAlias());
                oldDisk.setDiskDescription(newDisk.getDiskDescription());
                oldDisk.setShareable(newDisk.isShareable());
                DbFacade.getInstance().getBaseDiskDao().update(oldDisk);
                if (oldDisk.getDiskStorageType() == DiskStorageType.IMAGE) {
                    DiskImage diskImage = (DiskImage) oldDisk;
                    diskImage.setQuotaId(getQuotaId());
                    getImageDao().update(diskImage.getImage());
                }
                // update cached image
                VmHandler.updateDisksFromDb(getVm());
                // update vm device boot order
                VmDeviceUtils.updateBootOrderInVmDeviceAndStoreToDB(getVm().getStaticData());

                setSucceeded(true);
                return null;
            }

            private void clearAddressOnInterfaceChange() {
                // clear the disk address if the type has changed
                if (oldDisk.getDiskInterface() != newDisk.getDiskInterface()) {
                    getVmDeviceDao().clearDeviceAddress(getVmDeviceDao().get(new VmDeviceId(oldDisk.getId(), getVmId()))
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
        return oldDisk.getDiskAlias();
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

    private boolean isQuotaValidationNeeded() {
        return oldDisk != null && newDisk != null && oldDisk instanceof DiskImage &&
                newDisk instanceof DiskImage && DiskStorageType.IMAGE == oldDisk.getDiskStorageType();
    }

    private Guid getQuotaId() {
        if (newDisk != null && isQuotaValidationNeeded()) {
            return ((DiskImage) newDisk).getQuotaId();
        }
        return null;
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();

        if (isQuotaValidationNeeded()) {
            DiskImage oldDiskImage = (DiskImage) oldDisk;
            DiskImage newDiskImage = (DiskImage) newDisk;

            boolean emptyOldQuota = oldDiskImage.getQuotaId() == null || Guid.Empty.equals(oldDiskImage.getQuotaId());
            boolean differentNewQuota = !emptyOldQuota && !oldDiskImage.getQuotaId().equals(newDiskImage.getQuotaId());

            if (emptyOldQuota || differentNewQuota) {
                list.add(new QuotaStorageConsumptionParameter(
                    newDiskImage.getQuotaId(),
                    null,
                    QuotaStorageConsumptionParameter.QuotaAction.CONSUME,
                    //TODO: Shared Disk?
                    newDiskImage.getstorage_ids().get(0),
                    (double)newDiskImage.getSizeInGigabytes()));
            }

            if (differentNewQuota) {
                list.add(new QuotaStorageConsumptionParameter(
                    oldDiskImage.getQuotaId(),
                    null,
                    QuotaStorageConsumptionParameter.QuotaAction.RELEASE,
                    //TODO: Shared Disk?
                    oldDiskImage.getstorage_ids().get(0),
                    (double)oldDiskImage.getSizeInGigabytes()));
            }
        }
        return list;
    }

}
