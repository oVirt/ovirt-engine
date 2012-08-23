package org.ovirt.engine.core.bll;

import java.util.*;

import org.ovirt.engine.core.bll.quota.QuotaManager;
import org.ovirt.engine.core.common.PermissionSubject;
import org.ovirt.engine.core.common.Quotable;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.vdscommands.DestroyVmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UpdateVmDynamicDataVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public abstract class StopVmCommandBase<T extends VmOperationParameterBase> extends VmOperationCommandBase<T> implements Quotable {
    private boolean privateSuspendedVm;

    public StopVmCommandBase(T parameters) {
        super(parameters);
    }

    protected boolean getSuspendedVm() {
        return privateSuspendedVm;
    }

    private void setSuspendedVm(boolean value) {
        privateSuspendedVm = value;
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = true;
        if (getVm() == null) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        } else if (!VM.isStatusUp(getVm().getstatus()) && getVm().getstatus() != VMStatus.Paused
                && getVm().getstatus() != VMStatus.NotResponding && getVm().getstatus() != VMStatus.Suspended) {
            if (getVm().getstatus() == VMStatus.SavingState || getVm().getstatus() == VMStatus.RestoringState) {
                retValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_SAVING_RESTORING);
            } else {
                retValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_RUNNING);
            }
        }

        return retValue;
    }

    protected void Destroy() {
        if (getVm().getstatus() == VMStatus.MigratingFrom && getVm().getmigrating_to_vds() != null) {
            Backend.getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.DestroyVm,
                            new DestroyVmVDSCommandParameters(new Guid(getVm().getmigrating_to_vds().toString()),
                                    getVmId(), true, false, 0));
        }

        setActionReturnValue(Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.DestroyVm,
                        new DestroyVmVDSCommandParameters(getVdsId(), getVmId(), false, false, 0)).getReturnValue());
    }

    @Override
    protected void ExecuteVmCommand() {
        getParameters().setEntityId(getVm().getId());
        if (getVm().getstatus() == VMStatus.Suspended
                || !StringHelper.isNullOrEmpty(getVm().gethibernation_vol_handle())) {
            setSuspendedVm(true);
            setSucceeded(StopSuspendedVm());
        } else {
            super.ExecuteVmCommand();
        }
        removeStatelessVmUnmanagedDevices();
    }

    private void removeStatelessVmUnmanagedDevices() {
        if (getSucceeded() && getVm().getis_stateless()) {
            // remove all unmanaged devices of a stateless VM
            List<VmDevice> vmDevices =
                DbFacade.getInstance()
                        .getVmDeviceDAO()
                        .getUnmanagedDevicesByVmId(getVm().getId());
            for (VmDevice device : vmDevices) {
                // do not remove device if appears in white list
                if (! VmDeviceCommonUtils.isInWhiteList(device.getType(), device.getDevice())) {
                    DbFacade.getInstance().getVmDeviceDAO().remove(device.getId());
                }
            }
        }
    }

    /**
     * Start stopping operation for suspended VM, by deleting its storage image groups (Created by hibernation process
     * which indicated its saved memory), and set the VM status to image locked.
     *
     * @return True - Operation succeeded <BR/>
     *         False - Operation failed.
     */
    private boolean StopSuspendedVm() {
        boolean returnVal = false;

        // Set the Vm to null, for getting the recent VM from the DB, instead from the cache.
        setVm(null);
        VMStatus vmStatus = getVm().getstatus();

        // Check whether stop VM procedure didn't started yet (Status is not imageLocked), by another transaction.
        if (getVm().getstatus() != VMStatus.ImageLocked) {
            // Set the VM to image locked to decrease race condition.
            getVm().setstatus(VMStatus.ImageLocked);
            UpdateVmData(getVm().getDynamicData());
            if (!StringHelper.isNullOrEmpty(getVm().gethibernation_vol_handle())
                    && HandleHibernatedVm(getActionType(), false)) {
                returnVal = true;
            } else {
                getVm().setstatus(vmStatus);
                UpdateVmData(getVm().getDynamicData());
            }
        }
        return returnVal;
    }

    /**
     * Update Vm dynamic data in the DB.<BR/>
     * If VM is active in the VDSM (not suspended/stop), we will use UpdateVmDynamicData VDS command, for preventing
     * over write in the DB, otherwise , update directly to the DB.
     */
    private void UpdateVmData(VmDynamic vmDynamicData) {
        if (getVm().getrun_on_vds() != null) {
            Backend.getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.UpdateVmDynamicData,
                            new UpdateVmDynamicDataVDSCommandParameters(getVm().getrun_on_vds().getValue(),
                                    vmDynamicData));
        } else {
            DbFacade.getInstance().getVmDynamicDAO().update(vmDynamicData);
        }
    }

    @Override
    protected void EndVmCommand() {
        setCommandShouldBeLogged(false);

        if (getVm() != null) {
            getVm().setstatus(VMStatus.Down);
            getVm().sethibernation_vol_handle(null);

            DbFacade.getInstance().getVmDynamicDAO().update(getVm().getDynamicData());
        }

        else {
            log.warn("StopVmCommandBase::EndVmCommand: Vm is null - not performing full EndAction");
        }

        setSucceeded(true);
    }

    private static Log log = LogFactory.getLog(StopVmCommandBase.class);

    @Override
    public boolean validateAndSetQuota() {
        return true;
    }

    @Override
    public void rollbackQuota() {
        if (getStoragePool() == null) {
            setStoragePool(getStoragePoolDAO().getForVdsGroup(getVm().getvds_group_id()));
        }
        Guid quotaId = getVm().getQuotaId();
        Set<Guid> quotaIdsForRollback = new HashSet<Guid>();
        if (quotaId != null) {
            // Uncache the details of this quota. next time the quota will be called, a new calculation
            // would be done base on the DB.
            quotaIdsForRollback.add(quotaId);
        }
        VmHandler.updateDisksFromDb(getVm());
        for (DiskImage image : getVm().getDiskList()){
            quotaId = image.getQuotaId();

            if (quotaId != null) {
                quotaIdsForRollback.add(quotaId);
            }
        }
        QuotaManager.getInstance().rollbackQuota(getStoragePool(), new ArrayList<Guid>(quotaIdsForRollback));
    }

    @Override
    public Guid getQuotaId() {
        return null;
    }

    @Override
    public void addQuotaPermissionSubject(List<PermissionSubject> quotaPermissionList) {
        //
    }

    @Override
    protected void setSucceeded(boolean value) {
        super.setSucceeded(value);
        if (value) {
            rollbackQuota();
        }
    }
}
