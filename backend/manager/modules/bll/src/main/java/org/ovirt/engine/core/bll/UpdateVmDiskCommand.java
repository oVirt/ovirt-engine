package org.ovirt.engine.core.bll;

import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.PermissionSubject;
import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class UpdateVmDiskCommand<T extends UpdateVmDiskParameters> extends AbstractDiskVmCommand<T> {

    private static final long serialVersionUID = 5915267156998835363L;
    private List<PermissionSubject> listPermissionSubjects;
    private DiskImage _oldDisk;
    private boolean shouldUpdateQuotaForDisk;

    public UpdateVmDiskCommand(T parameters) {
        super(parameters);
        setQuotaId(parameters.getDiskInfo() != null ? parameters.getDiskInfo().getQuotaId() : null);
    }


    @Override
    protected void ExecuteVmCommand() {
        perforDiskUpdate();
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = isVmExist();
        if (retValue) {
            _oldDisk = getDiskImageDao().get(getParameters().getImageId());

            // Set disk alias name in the disk retrieved from the parameters.
            ImagesHandler.setDiskAlias(getParameters().getDiskInfo().getDisk(), getVm());
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
    protected DiskImageDAO getDiskImageDao() {
        return DbFacade.getInstance().getDiskImageDAO();
    }

    @Override
    protected boolean validateQuota() {
        boolean quotaValid = true;
        shouldUpdateQuotaForDisk = !_oldDisk.getQuotaId().equals(getQuotaId());
        if (shouldUpdateQuotaForDisk) {
            // Set default quota id if storage pool enforcement is disabled.
            getParameters().setQuotaId(QuotaHelper.getInstance().getQuotaIdToConsume(getQuotaId(),
                    getStoragePool()));
            setStorageDomainId(_oldDisk.getstorage_ids().get(0).getValue());
            quotaValid = (QuotaManager.validateStorageQuota(getStorageDomainId().getValue(),
                    getParameters().getQuotaId(),
                    getStoragePool().getQuotaEnforcementType(),
                    new Double(getParameters().getDiskInfo().getSizeInGigabytes()),
                    getCommandId(),
                    getReturnValue().getCanDoActionMessages()));
        }
        return quotaValid;
    }

    private boolean checkCanPerformRegularUpdate() {
        boolean retValue = true;
        if (VM.isStatusUpOrPausedOrSuspended(getVm().getstatus())) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL);
        } else if (_oldDisk.getdisk_interface() != getParameters().getDiskInfo().getdisk_interface()) {
            List<VmNetworkInterface> allVmInterfaces = DbFacade.getInstance()
                    .getVmNetworkInterfaceDAO().getAllForVm(getVmId());

            List allVmDisks = getDiskImageDao().getAllForVm(getVmId());
            allVmDisks.removeAll(LinqUtils.filter(allVmDisks, new Predicate<DiskImageBase>() {
                @Override
                public boolean eval(DiskImageBase o) {
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
        if (retValue && getParameters().getDiskInfo().getboot()) {
            VmHandler.updateDisksFromDb(getVm());
            for (DiskImage disk : getVm().getDiskMap().values()) {
                if (disk.getboot() && !getParameters().getImageId().equals(disk.getId())) {
                    retValue = false;
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_BOOT_IN_USE);
                    getReturnValue().getCanDoActionMessages().add(
                            String.format("$DiskName %1$s", disk.getinternal_drive_mapping()));
                    break;
                }
            }
        }
        return retValue;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        if (listPermissionSubjects == null) {
            listPermissionSubjects = super.getPermissionCheckSubjects();
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
                _oldDisk.setboot(getParameters().getDiskInfo().getboot());
                _oldDisk.setdisk_interface(getParameters().getDiskInfo().getdisk_interface());
                _oldDisk.setpropagate_errors(getParameters().getDiskInfo().getpropagate_errors());
                _oldDisk.setwipe_after_delete(getParameters().getDiskInfo().getwipe_after_delete());
                _oldDisk.setQuotaId(getQuotaId());
                _oldDisk.getDisk().setDiskAlias(getParameters().getDiskInfo().getDisk().getDiskAlias());
                DbFacade.getInstance().getBaseDiskDao().update(_oldDisk.getDisk());
                getDiskImageDao().update(_oldDisk);
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
}
