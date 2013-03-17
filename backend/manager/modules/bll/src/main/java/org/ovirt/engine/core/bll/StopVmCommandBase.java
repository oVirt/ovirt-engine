package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.quota.QuotaVdsGroupConsumptionParameter;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.DestroyVmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UpdateVmDynamicDataVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public abstract class StopVmCommandBase<T extends VmOperationParameterBase> extends VmOperationCommandBase<T>
        implements QuotaVdsDependent, QuotaStorageDependent {
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
        } else if (!getVm().isRunning() && getVm().getStatus() != VMStatus.Paused
                && getVm().getStatus() != VMStatus.NotResponding && getVm().getStatus() != VMStatus.Suspended) {
            if (getVm().getStatus().isHibernating() || getVm().getStatus() == VMStatus.RestoringState) {
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
        if (getVm().getStatus() == VMStatus.MigratingFrom && getVm().getMigratingToVds() != null) {
            Backend.getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.DestroyVm,
                            new DestroyVmVDSCommandParameters(new Guid(getVm().getMigratingToVds().toString()),
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
        if (getVm().getStatus() == VMStatus.Suspended
                || StringUtils.isNotEmpty(getVm().getHibernationVolHandle())) {
            setSuspendedVm(true);
            setSucceeded(stopSuspendedVm());
        } else {
            super.executeVmCommand();
        }
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
        VMStatus vmStatus = getVm().getStatus();

        // Check whether stop VM procedure didn't started yet (Status is not imageLocked), by another transaction.
        if (getVm().getStatus() != VMStatus.ImageLocked) {
            // Set the VM to image locked to decrease race condition.
            updateVmStatus(VMStatus.ImageLocked);
            if (StringUtils.isNotEmpty(getVm().getHibernationVolHandle())
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
                getCompensationContext().snapshotEntityStatus(getVm().getDynamicData(),getVm().getStatus());
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
        if (getVm().getRunOnVds() != null) {
            Backend.getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.UpdateVmDynamicData,
                            new UpdateVmDynamicDataVDSCommandParameters(getVm().getRunOnVds().getValue(),
                                    vmDynamicData));
        } else {
            DbFacade.getInstance().getVmDynamicDao().update(vmDynamicData);
        }
    }

    @Override
    protected void endVmCommand() {
        setCommandShouldBeLogged(false);

        if (getVm() != null) {
            getVm().setStatus(VMStatus.Down);
            getVm().setHibernationVolHandle(null);

            DbFacade.getInstance().getVmDynamicDao().update(getVm().getDynamicData());
        }

        else {
            log.warn("StopVmCommandBase::EndVmCommand: Vm is null - not performing full EndAction");
        }

        setSucceeded(true);
    }

    private static Log log = LogFactory.getLog(StopVmCommandBase.class);

    @Override
    public List<QuotaConsumptionParameter> getQuotaVdsConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();

        if (getVm().getQuotaId() != null && !Guid.Empty.equals(getVm().getQuotaId())
                && getQuotaManager().isVmStatusQuotaCountable(getVm().getStatus())) {
            list.add(new QuotaVdsGroupConsumptionParameter(getVm().getQuotaId(),
                    null,
                    QuotaConsumptionParameter.QuotaAction.RELEASE,
                    getVm().getVdsGroupId(),
                    getVm().getCpuPerSocket() * getVm().getNumOfSockets(),
                    getVm().getMemSizeMb()));
        }
        return list;
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();
        if (!getVm().isStateless()) {
            return list;
        }
        //if runAsStateless
        for (DiskImage image : getVm().getDiskList()) {
            if (image.getQuotaId() != null) {
                list.add(new QuotaStorageConsumptionParameter(image.getQuotaId(), null,
                        QuotaConsumptionParameter.QuotaAction.RELEASE,
                        image.getStorageIds().get(0), image.getActualSize()));
            }
        }
        return list;
    }

    @Override
    public void addQuotaPermissionSubject(List<PermissionSubject> quotaPermissionList) {
        //
    }
}
