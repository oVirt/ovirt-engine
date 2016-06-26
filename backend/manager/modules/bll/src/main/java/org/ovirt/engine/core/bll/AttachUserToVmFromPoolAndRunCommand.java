package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.quota.QuotaClusterConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.PermissionsOperationsParameters;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VdcActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmPoolUserParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPoolMap;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManager;

public class AttachUserToVmFromPoolAndRunCommand<T extends VmPoolUserParameters>
        extends VmPoolUserCommandBase<T> implements QuotaVdsDependent {

    @Inject
    private LockManager lockManager;

    private Set<Guid> lockedVms = new HashSet<>();

    private Guid vmToAttach;

    protected AttachUserToVmFromPoolAndRunCommand(Guid commandId) {
        super(commandId);
    }

    public AttachUserToVmFromPoolAndRunCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
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
    protected boolean validate() {
        boolean returnValue = true;

        synchronized (_lockObject) {
            // no available VMs:
            if (Guid.Empty.equals(getVmToAttach())) {
                addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_NO_AVAILABLE_POOL_VMS);
                returnValue = false;
            }
        }

        // check user isn't already attached to maximum number of vms from this pool
        if (returnValue) {
            List<VM> vmsForUser = getVmDao().getAllForUser(getAdUserId());

            int vmCount = 0;
            for (VM vm : vmsForUser) {
                if (vm.getVmPoolId() != null && getVmPoolId().equals(vm.getVmPoolId())) {
                    vmCount++;
                }
            }

            int limit = getVmPool().getMaxAssignedVmsPerUser();
            if (vmCount >= limit) {
                addValidationMessage(EngineMessage.VM_POOL_CANNOT_ATTACH_TO_MORE_VMS_FROM_POOL);
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
        addValidationMessage(EngineMessage.VAR__ACTION__ALLOCATE_AND_RUN);
        addValidationMessage(EngineMessage.VAR__TYPE__VM_FROM_VM_POOL);
    };

    private Guid getVmToAttach() {
        if (vmToAttach == null) {
            Guid vmGuid = getPrestartedVmToAttach();
            if (Guid.Empty.equals(vmGuid)) {
                vmGuid = getNonPrestartedVmToAttach();
            }
            vmToAttach = vmGuid;
        }
        return vmToAttach;
    }

    private Guid getPrestartedVmToAttach() {
        List<VmPoolMap> vmPoolMaps = getVmPoolDao().getVmMapsInVmPoolByVmPoolIdAndStatus(getVmPoolId(), VMStatus.Up);
        if (vmPoolMaps != null) {
            for (VmPoolMap map : vmPoolMaps) {
                if (!lockedVms.contains(map.getVmId())
                        && canAttachPrestartedVmToUser(map.getVmId(),
                                getVmPool().isStateful(),
                                getReturnValue().getValidationMessages())) {
                    return map.getVmId();
                }
            }
        }
        return Guid.Empty;
    }

    private Guid getNonPrestartedVmToAttach() {
        List<VmPoolMap> vmPoolMaps = getVmPoolDao().getVmMapsInVmPoolByVmPoolIdAndStatus(getVmPoolId(), VMStatus.Down);
        if (vmPoolMaps != null) {
            for (VmPoolMap map : vmPoolMaps) {
                if (!lockedVms.contains(map.getVmId())
                        && canAttachNonPrestartedVmToUser(map.getVmId(), getReturnValue().getValidationMessages())) {
                    return map.getVmId();
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
    public Guid getAdUserId() {
        return getParameters().getUserId();
    }

    @Override
    protected TransactionScopeOption getTransactionScopeOption() {
        return getActionState() != CommandActionState.EXECUTE ? TransactionScopeOption.Suppress
                : super.getTransactionScopeOption();
    }

    @Override
    protected void executeCommand() {
        initPoolUser();

        boolean isPrestartedVm = false;
        Guid vmToAttach;
        EngineLock vmLock;

        synchronized (_lockObject) {
            while (true) {
                // Find a VM to use
                vmToAttach = getPrestartedVmToAttach();
                if (!Guid.Empty.equals(vmToAttach)) {
                    isPrestartedVm = true;
                } else {
                    vmToAttach = getNonPrestartedVmToAttach();
                }

                // No VM available
                if (Guid.Empty.equals(vmToAttach)) {
                    log.info("No free Vms in pool '{}'. Cannot allocate for user '{}'", getVmPoolId(), getAdUserId());
                    throw new EngineException(EngineError.NO_FREE_VM_IN_POOL);
                }

                // Lock the VM
                vmLock = createEngineLockForRunVm(vmToAttach);
                if (acquireLock(vmLock)) {
                    break;
                } else {
                    lockedVms.add(vmToAttach); // VM is locked by another command, ignore it
                }
            }

            getParameters().setEntityInfo(new EntityInfo(VdcObjectType.VM, vmToAttach));
            setVmId(vmToAttach);

            VdcReturnValueBase vdcReturnValue = attachUserToVm(vmToAttach);

            if (!vdcReturnValue.getSucceeded()) {
                log.info("Failed to give user '{}' permission to Vm '{}'", getAdUserId(), vmToAttach);
                setActionReturnValue(vdcReturnValue);
                releaseLock(vmLock);
                return;
            } else {
                log.info("Succeeded giving user '{}' permission to Vm '{}'", getAdUserId(), vmToAttach);
            }
        }

        if (!isPrestartedVm) {
            // Only when using a VM that is not prestarted we need to run the VM
            setVm(getVmDao().get(vmToAttach));

            VdcReturnValueBase vdcReturnValue = runVm(vmToAttach, vmLock);

            setSucceeded(vdcReturnValue.getSucceeded());
            setActionReturnValue(vmToAttach);
            getReturnValue().getVdsmTaskIdList().addAll(getReturnValue().getInternalVdsmTaskIdList());
        } else {
            // No need to start, just return it
            setActionReturnValue(vmToAttach);
            setSucceeded(true);
            releaseLock(vmLock);
        }
    }

    private VdcReturnValueBase attachUserToVm(Guid vmId) {
        Permission perm = new Permission(getAdUserId(),
                PredefinedRoles.ENGINE_USER.getId(),
                vmId,
                VdcObjectType.VM);
        PermissionsOperationsParameters permParams = new PermissionsOperationsParameters(perm);
        permParams.setShouldBeLogged(false);
        permParams.setParentCommand(getActionType());
        permParams.setParentParameters(getParameters());
        return runInternalAction(VdcActionType.AddPermission,
                permParams,
                cloneContext().withoutExecutionContext().withoutLock());
    }

    private ExecutionContext createRunVmContext() {
        ExecutionContext ctx = new ExecutionContext();
        try {
            Step step = ExecutionHandler.addSubStep(getExecutionContext(),
                    getExecutionContext().getJob().getStep(StepEnum.EXECUTING),
                    StepEnum.TAKING_VM_FROM_POOL,
                    ExecutionMessageDirector.resolveStepMessage(StepEnum.TAKING_VM_FROM_POOL,
                            Collections.singletonMap(VdcObjectType.VM.name().toLowerCase(), getVmName())));
            ctx.setStep(step);
            ctx.setMonitored(true);
            ctx.setShouldEndJob(true);
        } catch (RuntimeException e) {
            log.error("Error when creating executing context for running stateless VM", e);
        }
        return ctx;
    }

    private VdcReturnValueBase runVm(Guid vmToAttach, EngineLock vmLock) {
        RunVmParams runVmParams = new RunVmParams(vmToAttach);
        runVmParams.setSessionId(getParameters().getSessionId());
        runVmParams.setEntityInfo(new EntityInfo(VdcObjectType.VM, vmToAttach));
        runVmParams.setParentCommand(getActionType());
        runVmParams.setParentParameters(getParameters());
        runVmParams.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        runVmParams.setRunAsStateless(!getVmPool().isStateful());
        ExecutionContext runVmContext = createRunVmContext();
        VdcReturnValueBase vdcReturnValue = runInternalAction(VdcActionType.RunVm,
                runVmParams,
                cloneContext().withExecutionContext(runVmContext).withLock(vmLock).withCompensationContext(null));

        getTaskIdList().addAll(vdcReturnValue.getInternalVdsmTaskIdList());
        return vdcReturnValue;
    }

    private RunVmParams getChildRunVmParameters() {
        if (getParameters().getImagesParameters() == null) {
            return null;
        }
        return (RunVmParams) getParameters().getImagesParameters()
                .stream()
                .filter(p -> p instanceof RunVmParams)
                .findFirst()
                .orElse(null);
    }

    private boolean isRunVmSucceeded() {
        RunVmParams runVmParams = getChildRunVmParameters();
        return runVmParams == null
                || CommandCoordinatorUtil.getCommandEntity(runVmParams.getCommandId()).getReturnValue().getSucceeded();
    }

    @Override
    protected void endSuccessfully() {
        RunVmParams runVmParams = getChildRunVmParameters();
        if (runVmParams != null) {
            setVmId(runVmParams.getVmId());
            if (!isRunVmSucceeded()) {
                log.warn("endSuccessfully: RunVm failed, detaching user from VM");
                detachUserFromVmFromPool();
                getReturnValue().setEndActionTryAgain(false);
            }
        } else {
            setCommandShouldBeLogged(false);
            log.warn("endSuccessfully: VM is null");
        }
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        RunVmParams runVmParams = getChildRunVmParameters();
        if (runVmParams != null) {
            setVmId(runVmParams.getVmId());
            log.warn("endWithFailure: RunVm failed, detaching user from VM");
            detachUserFromVmFromPool();
        } else {
            log.warn("endWithFailure: VM is null");
        }
        setSucceeded(true);
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

    private EngineLock createEngineLockForRunVm(Guid vmId) {
        return new EngineLock(
                RunVmCommandBase.getExclusiveLocksForRunVm(vmId, getLockMessage()),
                RunVmCommandBase.getSharedLocksForRunVm());
    }

    private String getLockMessage() {
        return EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED.name();
    }

    private boolean acquireLock(EngineLock lock) {
        return lockManager.acquireLock(lock).getFirst();
    }

    private void releaseLock(EngineLock lock) {
        lockManager.releaseLock(lock);
    }

    protected void detachUserFromVmFromPool() {
        if (!Guid.Empty.equals(getAdUserId())) {
            Permission perm = getPermissionDao()
                    .getForRoleAndAdElementAndObject(
                            PredefinedRoles.ENGINE_USER.getId(),
                            getAdUserId(),
                            getVmId());
            if (perm != null) {
                getPermissionDao().remove(perm.getId());
            }
        }
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getAdUserId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.USER_VM_POOL, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaVdsConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();
        if (vmToAttach != null) {
            VM vm = getVmDao().get(vmToAttach);

            setStoragePoolId(vm.getStoragePoolId());

            list.add(new QuotaClusterConsumptionParameter(vm.getQuotaId(),
                    null,
                    QuotaConsumptionParameter.QuotaAction.CONSUME,
                    vm.getClusterId(),
                    vm.getCpuPerSocket() * vm.getNumOfSockets(),
                    vm.getMemSizeMb()));
        }
        return list;
    }

    @Override
    public CommandCallback getCallback() {
        return new ConcurrentChildCommandsExecutionCallback();
    }

}
