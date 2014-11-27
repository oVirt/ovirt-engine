package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.quota.QuotaVdsGroupConsumptionParameter;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RemoveVmHibernationVolumesParameters;
import org.ovirt.engine.core.common.action.StopVmParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.DestroyVmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UpdateVmDynamicDataVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class StopVmCommandBase<T extends StopVmParametersBase> extends VmOperationCommandBase<T>
        implements QuotaVdsDependent, QuotaStorageDependent {
    private static final Logger log = LoggerFactory.getLogger(StopVmCommandBase.class);

    private boolean suspendedVm;

    protected StopVmCommandBase(Guid guid) {
        super(guid);
    }

    protected StopVmCommandBase(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setReason(parameters.getStopReason());
    }

    public StopVmCommandBase(T parameters) {
        this(parameters, null);
    }

    protected boolean getSuspendedVm() {
        return suspendedVm;
    }

    @Override
    protected boolean canDoAction() {
        if (shouldSkipCommandExecutionCached()) {
            return true;
        }

        if (getVm() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        if (!getVm().isRunning() && getVm().getStatus() != VMStatus.Paused
                && getVm().getStatus() != VMStatus.NotResponding && getVm().getStatus() != VMStatus.Suspended) {
            return failCanDoAction(
                    (getVm().getStatus().isHibernating() || getVm().getStatus() == VMStatus.RestoringState) ?
                            VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_SAVING_RESTORING
                            : VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_RUNNING);
        }

        return true;
    }

    protected void destroyVm() {
        boolean wasPaused = (getVm().getStatus() == VMStatus.Paused
                            && getVm().getVmPauseStatus() == VmPauseStatus.NOERR);

        if (getVm().getStatus() == VMStatus.MigratingFrom && getVm().getMigratingToVds() != null) {
            Backend.getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.DestroyVm,
                            new DestroyVmVDSCommandParameters(new Guid(getVm().getMigratingToVds().toString()),
                                    getVmId(), getParameters().getStopReason(), true, false, 0));
        }

        setActionReturnValue(Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.DestroyVm,
        new DestroyVmVDSCommandParameters(getVdsId(), getVmId(), getParameters().getStopReason(), false, false, 0)).getReturnValue());

        if (wasPaused) {
            VmHandler.decreasePendingVms(getVm().getStaticData(), getVdsId());
        }
    }

    @Override
    protected void executeVmCommand() {
        getParameters().setEntityInfo(new EntityInfo(VdcObjectType.VM, getVm().getId()));
        // Mark that the stopped vm was suspended before for audit log messages
        suspendedVm = getVm().getStatus() == VMStatus.Suspended;

        if (suspendedVm || StringUtils.isNotEmpty(getVm().getHibernationVolHandle())) {
            if (!stopSuspendedVm()) {
                return;
            }

            // the following is true when the hibernation volumes don't exist
            if (getTaskIdList().isEmpty()) {
                endVmCommand();
                setCommandShouldBeLogged(true);
            } else {
                setSucceeded(true);
            }
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
        // Set the Vm to null, for getting the recent VM from the DB, instead from the cache.
        setVm(null);
        final VMStatus vmStatus = getVm().getStatus();

        // Check whether stop VM procedure didn't started yet (Status is not imageLocked), by another transaction.
        if (vmStatus == VMStatus.ImageLocked) {
            return false;
        }

        // Set the VM to image locked to decrease race condition.
        updateVmStatus(VMStatus.ImageLocked);

        if (!removeVmHibernationVolumes()) {
            updateVmStatus(vmStatus);
            return false;
        }

        return true;
    }

    private boolean removeVmHibernationVolumes() {
        if (StringUtils.isEmpty(getVm().getHibernationVolHandle())) {
            return false;
        }

        RemoveVmHibernationVolumesParameters parameters = new RemoveVmHibernationVolumesParameters(getVmId());
        parameters.setParentCommand(getActionType());
        parameters.setEntityInfo(getParameters().getEntityInfo());
        parameters.setParentParameters(getParameters());

        VdcReturnValueBase vdcRetValue = runInternalActionWithTasksContext(
                VdcActionType.RemoveVmHibernationVolumes,
                parameters
                );

        if (vdcRetValue.getSucceeded()) {
            getReturnValue().getVdsmTaskIdList().addAll(vdcRetValue.getInternalVdsmTaskIdList());
        }

        return vdcRetValue.getSucceeded();
    }

    private void updateVmStatus(VMStatus newStatus) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                getCompensationContext().snapshotEntityStatus(getVm().getDynamicData());
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
                            new UpdateVmDynamicDataVDSCommandParameters(getVm().getRunOnVds(),
                                    vmDynamicData));
        } else {
            getVmDynamicDao().update(vmDynamicData);
        }
    }

    @Override
    protected void endVmCommand() {
        setCommandShouldBeLogged(false);

        if (getVm() != null) {
            getVm().setStatus(VMStatus.Down);
            getVm().setHibernationVolHandle(null);

            getVmDynamicDao().update(getVm().getDynamicData());
        }

        else {
            log.warn("StopVmCommandBase::EndVmCommand: Vm is null - not performing full endAction");
        }

        setSucceeded(true);
    }

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

    @Override
    protected  boolean shouldSkipCommandExecution() {
        return getVm() != null && getVm().getStatus() == VMStatus.Down;
    }

    protected AuditLogType logCommandExecutionSkipped(String actionName) {
        addCustomValue("Action", actionName);
        addCustomValue("VmStatus", getVm().getStatus().name());
        return AuditLogType.VM_ALREADY_IN_REQUESTED_STATUS;
    }
}
