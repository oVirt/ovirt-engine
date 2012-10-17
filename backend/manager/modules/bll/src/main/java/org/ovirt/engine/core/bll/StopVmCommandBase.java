package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.quota.QuotaVdsGroupConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.DestroyVmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UpdateVmDynamicDataVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public abstract class StopVmCommandBase<T extends VmOperationParameterBase> extends VmOperationCommandBase<T> implements QuotaVdsDependent {
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

    protected StopVmCommandBase(Guid guid) {
        super(guid);
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
    protected void executeVmCommand() {
        getParameters().setEntityId(getVm().getId());
        if (getVm().getstatus() == VMStatus.Suspended
                || !StringHelper.isNullOrEmpty(getVm().gethibernation_vol_handle())) {
            setSuspendedVm(true);
            setSucceeded(stopSuspendedVm());
        } else {
            super.executeVmCommand();
        }
        removeStatelessVmUnmanagedDevices();
    }

    private void removeStatelessVmUnmanagedDevices() {
        if (getSucceeded() && (getVm().getis_stateless() ||  isRunOnce())) {
            // remove all unmanaged devices of a stateless VM

            final List<VmDevice> vmDevices =
                    DbFacade.getInstance()
                            .getVmDeviceDao()
                            .getUnmanagedDevicesByVmId(getVm().getId());

            TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

                @Override
                public Void runInTransaction() {
                    for (VmDevice device : vmDevices) {
                        // do not remove device if appears in white list
                        if (! VmDeviceCommonUtils.isInWhiteList(device.getType(), device.getDevice())) {
                            DbFacade.getInstance().getVmDeviceDao().remove(device.getId());
                        }
                    }
                    return null;
                }

            });
        }
    }

    /*
     * This method checks if we are stopping a VM that was started by run-once
     * In such case we will may have 2 devices, one managed and one unmanaged for CD or Floppy
     * This is not supported currently by libvirt that allows only one CD/Floppy
     * This code should be removed if libvirt will support in future multiple CD/Floppy
     */
    private boolean isRunOnce() {
        List<VmDevice> cdList =
            DbFacade.getInstance()
                    .getVmDeviceDao().getVmDeviceByVmIdTypeAndDevice(getVm().getId(), VmDeviceType.DISK.getName(), VmDeviceType.CDROM.getName());
        List<VmDevice> floppyList =
            DbFacade.getInstance()
                    .getVmDeviceDao().getVmDeviceByVmIdTypeAndDevice(getVm().getId(), VmDeviceType.DISK.getName(), VmDeviceType.FLOPPY.getName());

        return (cdList.size() > 1 || floppyList.size() > 1);
    }

    /**
     * Start stopping operation for suspended VM, by deleting its storage image groups (Created by hibernation process
     * which indicated its saved memory), and set the VM status to image locked.
     *
     * @return True - Operation succeeded <BR/>
     *         False - Operation failed.
     */
    private boolean stopSuspendedVm() {
        boolean returnVal = false;

        // Set the Vm to null, for getting the recent VM from the DB, instead from the cache.
        setVm(null);
        VMStatus vmStatus = getVm().getstatus();

        // Check whether stop VM procedure didn't started yet (Status is not imageLocked), by another transaction.
        if (getVm().getstatus() != VMStatus.ImageLocked) {
            // Set the VM to image locked to decrease race condition.
            updateVmStatus(VMStatus.ImageLocked);
             if (!StringHelper.isNullOrEmpty(getVm().gethibernation_vol_handle())
                    && handleHibernatedVm(getActionType(), false)) {
                returnVal = true;
            } else {
                updateVmStatus(vmStatus);
            }
        }
        return returnVal;
    }

    private void updateVmStatus(VMStatus newStatus) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                getCompensationContext().snapshotEntityStatus(getVm().getDynamicData(),getVm().getstatus());
                updateVmData(getVm().getDynamicData());
                getCompensationContext().stateChanged();
                return null;
            }
        });
    }

    /**
     * Update Vm dynamic data in the DB.<BR/>
     * If VM is active in the VDSM (not suspended/stop), we will use UpdateVmDynamicData VDS command, for preventing
     * over write in the DB, otherwise , update directly to the DB.
     */
    private void updateVmData(VmDynamic vmDynamicData) {
        if (getVm().getrun_on_vds() != null) {
            Backend.getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.UpdateVmDynamicData,
                            new UpdateVmDynamicDataVDSCommandParameters(getVm().getrun_on_vds().getValue(),
                                    vmDynamicData));
        } else {
            DbFacade.getInstance().getVmDynamicDao().update(vmDynamicData);
        }
    }

    @Override
    protected void endVmCommand() {
        setCommandShouldBeLogged(false);

        if (getVm() != null) {
            getVm().setstatus(VMStatus.Down);
            getVm().sethibernation_vol_handle(null);

            DbFacade.getInstance().getVmDynamicDao().update(getVm().getDynamicData());
        }

        else {
            log.warn("StopVmCommandBase::EndVmCommand: Vm is null - not performing full EndAction");
        }

        setSucceeded(true);
    }

    private static Log log = LogFactory.getLog(StopVmCommandBase.class);

    public List<QuotaConsumptionParameter> getQuotaVdsConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();

        if (getVm().getQuotaId() != null && !Guid.Empty.equals(getVm().getQuotaId())
                && (getVm().getstatus() == VMStatus.Up
                || getVm().getstatus() == VMStatus.Paused
                || getVm().getstatus() == VMStatus.PoweringUp
                ||getVm().getstatus() == VMStatus.RebootInProgress)) {
            list.add(new QuotaVdsGroupConsumptionParameter(getVm().getQuotaId(),
                    null,
                    QuotaConsumptionParameter.QuotaAction.RELEASE,
                    getVm().getvds_group_id(),
                    getVm().getcpu_per_socket() * getVm().getnum_of_sockets(),
                    getVm().getmem_size_mb()));
        }
        return list;
    }

    @Override
    public void addQuotaPermissionSubject(List<PermissionSubject> quotaPermissionList) {
        //
    }
}
