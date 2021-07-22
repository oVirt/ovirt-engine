package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.quota.QuotaClusterConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachUserToVmFromPoolAndRunParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.PermissionsOperationsParameters;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.dao.DbUserDao;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.lock.EngineLock;

public class AttachUserToVmFromPoolAndRunCommand<T extends AttachUserToVmFromPoolAndRunParameters>
        extends VmPoolUserCommandBase<T> implements QuotaVdsDependent {

    @Inject
    private VmPoolHandler vmPoolHandler;
    @Inject
    private PermissionDao permissionDao;
    @Inject
    private DbUserDao dbUserDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;
    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    protected AttachUserToVmFromPoolAndRunCommand(Guid commandId) {
        super(commandId);
    }

    public AttachUserToVmFromPoolAndRunCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void init() {
        super.init();

        if (Guid.Empty.equals(getParameters().getVmId()) && getVmPool() != null) {
            boolean vmPrestarted = true;
            Guid vmToAttach = vmPoolHandler.selectPrestartedVm(
                    getVmPoolId(),
                    getVmPool().isStateful(),
                    (vmId, errors) -> getReturnValue().getValidationMessages().addAll(errors));
            if (Guid.Empty.equals(vmToAttach)) {
                vmPrestarted = false;
                vmToAttach = vmPoolHandler.selectNonPrestartedVm(
                        getVmPoolId(),
                        (vmId, errors) -> getReturnValue().getValidationMessages().addAll(errors));
            }
            getParameters().setVmId(vmToAttach);
            getParameters().setEntityInfo(new EntityInfo(VdcObjectType.VM, vmToAttach));
            getParameters().setVmPrestarted(vmPrestarted);
            getParameters().setNonPrestartedVmLocked(!vmPrestarted);
        }

        setVmId(getParameters().getVmId());
    }

    @Override
    protected boolean validate() {
        if (getVmPool() == null) {
            return failValidation(EngineMessage.VM_POOL_NOT_FOUND);
        }

        if (Guid.Empty.equals(getVmId())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_NO_AVAILABLE_POOL_VMS);
        }

        long vmCount = countVmsAssignedToUser();
        int limit = getVmPool().getMaxAssignedVmsPerUser();
        if (vmCount >= limit) {
            return failValidation(EngineMessage.VM_POOL_CANNOT_ATTACH_TO_MORE_VMS_FROM_POOL);
        }

        return true;
    }

    private long countVmsAssignedToUser() {
        return vmDao.getAllForUser(getAdUserId())
                .stream()
                .filter(vm -> vm.getVmPoolId() != null && getVmPoolId().equals(vm.getVmPoolId()))
                .count();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__ALLOCATE_AND_RUN);
        addValidationMessage(EngineMessage.VAR__TYPE__VM_FROM_VM_POOL);
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

    private boolean isVmPrestarted() {
        return getParameters().isVmPrestarted();
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

    private void initPoolUser() {
        DbUser user = getDbUser();
        if (user != null && user.getId() == null) {
            user.setId(Guid.newGuid());
            dbUserDao.save(user);
        }
    }

    @Override
    protected void executeCommand() {
        initPoolUser();

        ActionReturnValue actionReturnValue = attachUserToVm();

        if (!actionReturnValue.getSucceeded()) {
            log.info("Failed to give user '{}' permission to Vm '{}'", getAdUserId(), getVmId());
            setActionReturnValue(actionReturnValue);
            return;
        } else {
            log.info("Succeeded giving user '{}' permission to Vm '{}'", getAdUserId(), getVmId());
        }

        if (!isVmPrestarted()) {
            // Only when using a VM that is not prestarted we need to run the VM
            actionReturnValue = runVm();

            setSucceeded(actionReturnValue.getSucceeded());
            getReturnValue().getVdsmTaskIdList().addAll(getReturnValue().getInternalVdsmTaskIdList());
        } else {
            // No need to start, just return it
            setSucceeded(true);
        }

        setActionReturnValue(getVmId());
    }

    private ActionReturnValue attachUserToVm() {
        Permission perm = new Permission(getAdUserId(),
                PredefinedRoles.ENGINE_USER.getId(),
                getVmId(),
                VdcObjectType.VM);
        PermissionsOperationsParameters permParams = new PermissionsOperationsParameters(perm);
        permParams.setShouldBeLogged(false);
        permParams.setParentCommand(getActionType());
        permParams.setParentParameters(getParameters());
        return runInternalAction(ActionType.AddPermission,
                permParams,
                cloneContext().withoutExecutionContext().withoutLock());
    }

    private ExecutionContext createRunVmContext() {
        ExecutionContext ctx = new ExecutionContext();
        try {
            Step step = executionHandler.addSubStep(getExecutionContext(),
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

    private ActionReturnValue runVm() {
        RunVmParams runVmParams = new RunVmParams(getVmId());
        runVmParams.setSessionId(getParameters().getSessionId());
        runVmParams.setEntityInfo(new EntityInfo(VdcObjectType.VM, getVmId()));
        runVmParams.setParentCommand(getActionType());
        runVmParams.setParentParameters(getParameters());
        runVmParams.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        runVmParams.setRunAsStateless(!getVmPool().isStateful());
        ExecutionContext runVmContext = createRunVmContext();
        EngineLock runVmLock = vmPoolHandler.createLock(getVmId());
        ActionReturnValue actionReturnValue = runInternalAction(ActionType.RunVm,
                runVmParams,
                cloneContext().withExecutionContext(runVmContext).withoutCompensationContext().withLock(runVmLock));

        getTaskIdList().addAll(actionReturnValue.getInternalVdsmTaskIdList());
        return actionReturnValue;
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
                || commandCoordinatorUtil.getCommandEntity(runVmParams.getCommandId()).getReturnValue().getSucceeded();
    }

    @Override
    protected void endSuccessfully() {
        if (!Guid.Empty.equals(getVmId())) {
            if (!isRunVmSucceeded()) {
                log.warn("endSuccessfully: RunVm failed, detaching user from VM");
                detachUserFromVmFromPool();
                getReturnValue().setEndActionTryAgain(false);
            }
        } else {
            setCommandShouldBeLogged(false);
            log.warn("endSuccessfully: VM is not set");
        }
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        if (!Guid.Empty.equals(getVmId())) {
            log.warn("endWithFailure: RunVm failed, detaching user from VM");
            detachUserFromVmFromPool();
        } else {
            log.warn("endWithFailure: VM is not set");
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

    private void detachUserFromVmFromPool() {
        if (!Guid.Empty.equals(getAdUserId())) {
            Permission perm = permissionDao
                    .getForRoleAndAdElementAndObject(
                            PredefinedRoles.ENGINE_USER.getId(),
                            getAdUserId(),
                            getVmId());
            if (perm != null) {
                permissionDao.remove(perm.getId());
            }
        }
    }

    @Override
    protected void freeLock() {
        super.freeLock();
        if (getCommandStatus() == CommandStatus.ENDED_WITH_FAILURE && !Guid.Empty.equals(getVmId())
                && getParameters().isNonPrestartedVmLocked()) {
            EngineLock runLock = vmPoolHandler.createLock(getVmId());
            lockManager.releaseLock(runLock);
            getParameters().setNonPrestartedVmLocked(false);
            log.info("VM lock freed: {}", runLock);
        }
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Map<String, Pair<String, String>> locks = new HashMap<>();
        locks.put(getAdUserId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.USER_VM_POOL, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        if (!Guid.Empty.equals(getVmId()) && isVmPrestarted()) {
            EngineLock runLock = vmPoolHandler.createLock(getVmId());
            if (runLock.getExclusiveLocks() != null) {
                locks.putAll(runLock.getExclusiveLocks());
            }
        }
        return locks;
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        if (!Guid.Empty.equals(getVmId()) && isVmPrestarted()) {
            return vmPoolHandler.createLock(getVmId()).getSharedLocks();
        } else {
            return null;
        }
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaVdsConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();
        VM vm = getVm();
        if (vm != null) {
            setStoragePoolId(vm.getStoragePoolId());

            list.add(new QuotaClusterConsumptionParameter(vm.getQuotaId(),
                    QuotaConsumptionParameter.QuotaAction.CONSUME,
                    vm.getClusterId(),
                    VmCpuCountHelper.isDynamicCpuTopologySet(vm) ? // might be true only for pre-started VMs
                            vm.getCurrentCoresPerSocket() * vm.getCurrentSockets() :
                            vm.getCpuPerSocket() * vm.getNumOfSockets(),
                    vm.getMemSizeMb()));
        }
        return list;
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

}
