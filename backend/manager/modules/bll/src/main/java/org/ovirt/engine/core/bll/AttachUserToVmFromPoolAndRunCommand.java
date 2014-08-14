package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.quota.QuotaVdsGroupConsumptionParameter;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.PermissionsOperationsParameters;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmPoolUserParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPoolMap;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;

public class AttachUserToVmFromPoolAndRunCommand<T extends VmPoolUserParameters> extends
VmPoolUserCommandBase<T> implements QuotaVdsDependent {
    private Guid vmToAttach = null;

    protected AttachUserToVmFromPoolAndRunCommand(Guid commandId) {
        super(commandId);
    }

    public AttachUserToVmFromPoolAndRunCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    /**
     * This lock is used to synchronize multiple users trying to attach a VM from pool, so that they won't be able to
     * attach the same VM to more than one user.
     */
    private static final Object _lockObject = new Object();

    @Override
    protected boolean canDoAction() {
        boolean returnValue = true;

        synchronized (_lockObject) {
            // no available VMs:
            if (Guid.Empty.equals(getVmToAttach(getParameters().getVmPoolId())))
            {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NO_AVAILABLE_POOL_VMS);
                returnValue = false;
            }
        }

        // check user isn't already attached to maximum number of vms from this pool
        if (returnValue) {
            List<VM> vmsForUser = getVmDAO().getAllForUser(getAdUserId());

            int vmCount = 0;
            for (VM vm : vmsForUser) {
                if (vm.getVmPoolId() != null && getVmPoolId().equals(vm.getVmPoolId())) {
                    vmCount++;
                }
            }

            int limit = getVmPool().getMaxAssignedVmsPerUser();
            if (vmCount >= limit) {
                addCanDoActionMessage(VdcBllMessages.VM_POOL_CANNOT_ATTACH_TO_MORE_VMS_FROM_POOL);
                returnValue = false;
            }
        }
        if (!returnValue) {
            setActionMessageParameters();
        }
        return returnValue;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ALLOCATE_AND_RUN);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_FROM_VM_POOL);
    };

    private Guid getVmToAttach(Guid poolId) {
        if (vmToAttach == null) {
            Guid vmGuid = getPrestartedVmToAttach(poolId);
            if (vmGuid == null || Guid.Empty.equals(vmGuid)) {
                vmGuid = getNonPrestartedVmToAttach(poolId);
            }
            vmToAttach = vmGuid;
        }
        return vmToAttach;
    }

    private Guid getPrestartedVmToAttach(Guid vmPoolId) {
        List<VmPoolMap> vmPoolMaps = getVmPoolDAO().getVmMapsInVmPoolByVmPoolIdAndStatus(vmPoolId, VMStatus.Up);
        if (vmPoolMaps != null) {
            for (VmPoolMap map : vmPoolMaps) {
                if (canAttachPrestartedVmToUser(map.getvm_guid())) {
                    return map.getvm_guid();
                }
            }
        }
        return Guid.Empty;
    }

    private Guid getNonPrestartedVmToAttach(Guid vmPoolId) {
        List<VmPoolMap> vmPoolMaps = getVmPoolDAO().getVmMapsInVmPoolByVmPoolIdAndStatus(vmPoolId, VMStatus.Down);
        if (vmPoolMaps != null) {
            for (VmPoolMap map : vmPoolMaps) {
                if (canAttachNonPrestartedVmToUser(map.getvm_guid())) {
                    return map.getvm_guid();
                }
            }
        }
        return Guid.Empty;
    }

    @Override
    public Guid getVmId() {
        return getParameters().getVmId();
    }

    @Override
    public void setVmId(Guid value) {
        super.setVmId(value);
        getParameters().setVmId(value);
    }

    @Override
    protected Guid getVmPoolId() {
        return getParameters().getVmPoolId();
    }

    @Override
    protected void setVmPoolId(Guid value) {
        getParameters().setVmPoolId(value);
    }

    @Override
    public Guid getAdUserId() {
        return getParameters().getUserId();
    }

    @Override
    protected TransactionScopeOption getTransactionScopeOption() {
        return getActionState() != CommandActionState.EXECUTE ? TransactionScopeOption.Suppress : super
                .getTransactionScopeOption();
    }

    @Override
    protected void executeCommand() {
        getParameters().setParentCommand(VdcActionType.AttachUserToVmFromPoolAndRun);

        initUser();
        boolean isPrestartedVm = false;
        Guid vmToAttach;
        synchronized (_lockObject) {
            vmToAttach = getPrestartedVmToAttach(getParameters().getVmPoolId());
            if (!Guid.Empty.equals(vmToAttach)) {
                isPrestartedVm = true;
            } else {
                vmToAttach = getNonPrestartedVmToAttach(getParameters().getVmPoolId());
            }

            if (!Guid.Empty.equals(vmToAttach)) {
                getParameters().setEntityInfo(new EntityInfo(VdcObjectType.VM, vmToAttach));
                setVmId(vmToAttach);
                Permissions perm = new Permissions(getAdUserId(), PredefinedRoles.ENGINE_USER.getId(), vmToAttach,
                        VdcObjectType.VM);
                PermissionsOperationsParameters permParams = new PermissionsOperationsParameters(perm);
                permParams.setShouldBeLogged(false);
                permParams.setParentCommand(VdcActionType.AttachUserToVmFromPoolAndRun);
                VdcReturnValueBase vdcReturnValueFromAddPerm = runInternalAction(VdcActionType.AddPermission,
                        permParams,
                        cloneContext().withoutExecutionContext().withoutLock());
                if (!vdcReturnValueFromAddPerm.getSucceeded()) {
                    log.infoFormat("Failed to give user {0} permission to Vm {1} ", getAdUserId(), vmToAttach);
                    setActionReturnValue(vdcReturnValueFromAddPerm);
                    return;
                } else {
                    log.infoFormat("Succceeded giving user {0} permission to Vm {1} ", getAdUserId(), vmToAttach);
                }
            } else {
                log.infoFormat("No free Vms in pool {0}. Cannot allocate for user {1} ", getVmPoolId(), getAdUserId());
                throw new VdcBLLException(VdcBllErrors.NO_FREE_VM_IN_POOL);
            }
        }

        // Only when using a Vm that is not prestarted do we need to run the vm
        if (!isPrestartedVm) {
            setVm(getVmDAO().get(vmToAttach));
            RunVmParams runVmParams = new RunVmParams(vmToAttach);
            runVmParams.setSessionId(getParameters().getSessionId());
            runVmParams.setParentParameters(getParameters());
            runVmParams.setEntityInfo(new EntityInfo(VdcObjectType.VM, vmToAttach));
            runVmParams.setParentCommand(VdcActionType.AttachUserToVmFromPoolAndRun);
            runVmParams.setRunAsStateless(true);
            ExecutionContext runVmContext = createRunVmContext();
            VdcReturnValueBase vdcReturnValue = runInternalAction(VdcActionType.RunVm,
                    runVmParams, cloneContext().withExecutionContext(runVmContext).withoutLock().withCompensationContext(null));

            getTaskIdList().addAll(vdcReturnValue.getInternalVdsmTaskIdList());
            setSucceeded(vdcReturnValue.getSucceeded());
            setActionReturnValue(vmToAttach);
            getReturnValue().getVdsmTaskIdList().addAll(getReturnValue().getInternalVdsmTaskIdList());
        } else {
            // no need to start, just return it
            setActionReturnValue(vmToAttach);
            setSucceeded(true);
        }
    }

    private ExecutionContext createRunVmContext() {
        ExecutionContext ctx = new ExecutionContext();
        try {
            Step step = ExecutionHandler.addSubStep(getExecutionContext(),
                    getExecutionContext().getJob().getStep(StepEnum.EXECUTING),
                    StepEnum.TAKING_VM_FROM_POOL,
                    ExecutionMessageDirector.resolveStepMessage(StepEnum.TAKING_VM_FROM_POOL, Collections.singletonMap(VdcObjectType.VM.name().toLowerCase(), getVmName())));
            ctx.setStep(step);
            ctx.setMonitored(true);
            ctx.setShouldEndJob(true);
        } catch (RuntimeException e) {
            log.error("Error when creating executing context for running stateless VM", e);
        }
        return ctx;
    }

    @Override
    protected void endSuccessfully() {
        if (getVm() != null) {
            if (DbFacade.getInstance().getSnapshotDao().exists(getVm().getId(), SnapshotType.STATELESS)) {
                setSucceeded(Backend.getInstance().endAction(VdcActionType.RunVm,
                        getParameters().getImagesParameters().get(0), cloneContext().withoutLock().withoutExecutionContext()).getSucceeded());

                if (!getSucceeded()) {
                    log.warn("EndSuccessfully: endAction of RunVm failed, detaching user from Vm");
                    detachUserFromVmFromPool(); // just in case.
                    getReturnValue().setEndActionTryAgain(false);
                }
            }
            else {
                // Pool-snapshot is gone (probably due to processVmPoolOnStopVm
                // treatment) ->
                // no point in running the VM or trying to run again the endAction
                // method:
                log.warn("EndSuccessfully: No images were created for Vm, detaching user from Vm");
                detachUserFromVmFromPool(); // just in case.
                getReturnValue().setEndActionTryAgain(false);
            }
        } else {
            setCommandShouldBeLogged(false);
            log.warn("AttachUserToVmFromPoolAndRunCommand::EndSuccessfully: Vm is null - not performing full endAction");
            setSucceeded(true);
        }
    }

    @Override
    protected void endWithFailure() {
        setSucceeded(Backend.getInstance().endAction(VdcActionType.RunVm,
                getParameters().getImagesParameters().get(0),
                cloneContext().withoutExecutionContext().withoutLock()).getSucceeded());
        if (!getSucceeded()) {
            log.warn("AttachUserToVmFromPoolAndRunCommand::EndWitFailure: endAction of RunVm Failed");
        }
        detachUserFromVmFromPool();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.USER_ATTACH_USER_TO_VM_FROM_POOL
                    : AuditLogType.USER_ATTACH_USER_TO_VM_FROM_POOL_FAILED;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.USER_ATTACH_USER_TO_VM_FROM_POOL_FINISHED_SUCCESS
                    : AuditLogType.USER_ATTACH_USER_TO_VM_FROM_POOL_FINISHED_FAILURE;

        default:
            return AuditLogType.USER_ATTACH_USER_TO_VM_FROM_POOL_FINISHED_FAILURE;
        }
    }

    protected void detachUserFromVmFromPool() {
        // Detach user from vm from pool:
        if (!Guid.Empty.equals(getAdUserId())) {
            Permissions perm = DbFacade
                    .getInstance()
                    .getPermissionDao()
                    .getForRoleAndAdElementAndObject(
                            PredefinedRoles.ENGINE_USER.getId(), getAdUserId(),
                            getVmId());
            if (perm != null) {
                DbFacade.getInstance().getPermissionDao().remove(perm.getId());
            }
        }
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getAdUserId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.USER_VM_POOL, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaVdsConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();
        if (vmToAttach != null) {
            VM vm = getVmDAO().get(vmToAttach);

            setStoragePoolId(vm.getStoragePoolId());

            list.add(new QuotaVdsGroupConsumptionParameter(vm.getQuotaId(),
                    null,
                    QuotaConsumptionParameter.QuotaAction.CONSUME,
                    vm.getVdsGroupId(),
                    vm.getCpuPerSocket() * vm.getNumOfSockets(),
                    vm.getMemSizeMb()));
        }
        return list;
    }
}
