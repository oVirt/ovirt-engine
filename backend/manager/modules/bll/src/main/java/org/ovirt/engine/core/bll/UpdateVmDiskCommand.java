package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@CustomLogFields({ @CustomLogField("DiskAlias") })
@NonTransactiveCommandAttribute
public class UpdateVmDiskCommand<T extends UpdateVmDiskParameters> extends AbstractDiskVmCommand<T> {

    private static final long serialVersionUID = 5915267156998835363L;
    private List<PermissionSubject> listPermissionSubjects;
    private final Disk _oldDisk;
    private boolean shouldUpdateQuotaForDisk;

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
            // Set disk alias name in the disk retrieved from the parameters.
            ImagesHandler.setDiskAlias(getParameters().getDiskInfo(), getVm());
            retValue = isDiskExist(_oldDisk) && checkCanPerformRegularUpdate();
        }
        return retValue;
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
        if (VM.isStatusUpOrPausedOrSuspended(getVm().getstatus())) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL);
        } else if (_oldDisk.getDiskInterface() != getParameters().getDiskInfo().getDiskInterface()) {
            List<VmNetworkInterface> allVmInterfaces = DbFacade.getInstance()
                    .getVmNetworkInterfaceDAO().getAllForVm(getVmId());

            List<Disk> allVmDisks = getDiskDao().getAllForVm(getVmId());
            allVmDisks.removeAll(LinqUtils.filter(allVmDisks, new Predicate<Disk>() {
                @Override
                public boolean eval(Disk o) {
                    return o.getinternal_drive_mapping().equals(
                            _oldDisk.getinternal_drive_mapping());
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
                            String.format("$DiskName %1$s", disk.getinternal_drive_mapping()));
                    break;
                }
            }

            // If disk is shareable and it is also bootable return an appropriate CDA message.
            if (getParameters().getDiskInfo().isBoot() && getParameters().getDiskInfo().isShareable()) {
                addCanDoActionMessage(VdcBllMessages.SHAREABLE_DISK_IS_NOT_SUPPORTED_FOR_DISK);
                return false;
            }
        }
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
            ((DiskImage)_oldDisk).setvm_snapshot_id(null);

            // TODO : After getForImageGroup will return list of vms, add a check for number of vms.
            List<DiskImage> diskImageList =
                    getDiskImageDao().getAllSnapshotsForImageGroup(_oldDisk.getId());

            // If disk image list is more then one then we assume that it has a snapshot, since one image is the active
            // disk and all the other images are the snapshots.
            if ((diskImageList.size() > 1) || !Guid.Empty.equals(((DiskImage)_oldDisk).getit_guid())) {
                addCanDoActionMessage(VdcBllMessages.SHAREABLE_DISK_IS_NOT_SUPPORTED_FOR_DISK);
                return false;
            }

            // Set allow snapshot attribute to be false when disk updated to shareable.
            _oldDisk.setAllowSnapshot(Boolean.FALSE);
        } else if (isDiskShareable && !isDiskUpdatedToShareable) {
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
        });
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_VM_DISK : AuditLogType.USER_FAILED_UPDATE_VM_DISK;
    }

    public String getDiskAlias() {
        return _oldDisk.getDiskAlias();
    }
}
