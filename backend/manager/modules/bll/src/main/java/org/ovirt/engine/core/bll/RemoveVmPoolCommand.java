package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.RemoveVmFromPoolParameters;
import org.ovirt.engine.core.common.action.RemoveVmParameters;
import org.ovirt.engine.core.common.action.StopVmParameters;
import org.ovirt.engine.core.common.action.StopVmTypeEnum;
import org.ovirt.engine.core.common.action.VmPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmPoolDao;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonTransactiveCommandAttribute
public class RemoveVmPoolCommand<T extends VmPoolParametersBase> extends VmPoolCommandBase<T> {

    private static final Logger log = LoggerFactory.getLogger(RemoveVmPoolCommand.class);

    @Inject
    private VmPoolDao vmPoolDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;
    @Inject
    @Typed(RemoveVmPoolCommandCallback.class)
    private Instance<RemoveVmPoolCommandCallback> callbackProvider;

    private List<VM> cachedVmsInPool;
    private Set<Guid> vmsRemoved = new HashSet<>();
    private boolean allVmsDown;

    public RemoveVmPoolCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void init() {
        super.init();

        if (getVmPool() != null) {
            setClusterId(getVmPool().getClusterId());
        }
        getParameters().setVmPoolName(getVmPoolName());
    }

    private List<VM> getCachedVmsInPool() {
        if (cachedVmsInPool == null && getVmPoolId() != null) {
            cachedVmsInPool = vmDao.getAllForVmPool(getVmPoolId());
        }
        return cachedVmsInPool;
    }

    @Override
    protected boolean validate() {
        if (getVmPool() == null) {
            return failValidation(EngineMessage.VM_POOL_NOT_FOUND);
        }
        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        addValidationMessage(EngineMessage.VAR__TYPE__DESKTOP_POOL);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Execution);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Map<String, Pair<String, String>> locks = new HashMap<>();
        locks.put(getVmPoolId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_POOL,
                        new LockMessage(EngineMessage.ACTION_TYPE_FAILED_VM_POOL_IS_BEING_REMOVED)
                                .withOptional("VmPoolName", getVmPool() != null ? getVmPool().getName() : null)));
        for (VM vm : getCachedVmsInPool()) {
            addVmLocks(vm, locks);
        }
        return locks;
    }

    private void addVmLocks(VM vm, Map<String, Pair<String, String>> locks) {
        locks.put(vm.getId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, getVmIsBeingRemovedMessage(vm)));
    }

    private LockMessage getVmIsBeingRemovedMessage(VM vm) {
        return new LockMessage(EngineMessage.ACTION_TYPE_FAILED_VM_POOL_IS_BEING_REMOVED_WITH_VM)
                .withOptional("VmPoolName", getVmPool() != null ? getVmPool().getName() : null)
                .withOptional("VmName", getVmPool() != null ? vm.getName() : null);
    }

    @Override
    protected void executeCommand() {
        TransactionSupport.executeInNewTransaction(() -> {
            getCompensationContext().snapshotEntity(getVmPool());
            setPoolBeingDestroyed();
            setPrestartedToZero();
            getCompensationContext().stateChanged();
            return null;
        });
        setCommandShouldBeLogged(false);  // disable logging at the end of command execution
        log();                            // and log the message now
        stopVms();
        if (allVmsDown) {
            endSuccessfully();
        } else {
            setSucceeded(true);
        }
    }

    private void setPoolBeingDestroyed() {
        vmPoolDao.setBeingDestroyed(getVmPoolId(), true);
    }

    private void setPrestartedToZero() {
        VmPool vmPool = getVmPool();
        if (vmPool.getPrestartedVms() > 0) {
            vmPool.setPrestartedVms(0);
            vmPoolDao.update(vmPool);
        }
    }

    private void stopVms() {
        allVmsDown = true;

        for (VM vm : getCachedVmsInPool()) {
            if (!vm.isDown()) {
                commandCoordinatorUtil.executeAsyncCommand(
                        ActionType.StopVm,
                        withRootCommandInfo(new StopVmParameters(vm.getId(), StopVmTypeEnum.NORMAL)),
                        cloneContextAndDetachFromParent());
                allVmsDown = false;
            } else {
                removeVm(vm);
            }
        }
    }

    @Override
    protected void endSuccessfully() {
        setSucceeded(removeAllVmsInPool() && removeVmPool());
        log();
    }

    @Override
    protected void endWithFailure() {
        setSucceeded(true);
        log();
    }

    private boolean removeVm(VM vm) {
        RemoveVmFromPoolParameters removeVmFromPoolParameters =
                new RemoveVmFromPoolParameters(vm.getId(), false, false);
        removeVmFromPoolParameters.setTransactionScopeOption(TransactionScopeOption.Suppress);
        ActionReturnValue result = runInternalActionWithTasksContext(
                ActionType.RemoveVmFromPool,
                removeVmFromPoolParameters);
        if (!result.getSucceeded()) {
            return false;
        }

        result = runInternalAction(
                ActionType.RemoveVm,
                new RemoveVmParameters(vm.getId(), false),
                createRemoveVmStepContext(vm));
        if (!result.getSucceeded()) {
            return false;
        }

        vmsRemoved.add(vm.getId());

        return true;
    }

    private boolean removeAllVmsInPool() {
        for (VM vm : getCachedVmsInPool()) {
            if (!vmsRemoved.contains(vm.getId()) && !removeVm(vm)) {
                return false;
            }
        }

        return true;
    }

    private boolean canRemoveVmPool(Guid vmPoolId) {
        return vmPoolDao.getVmPoolsMapByVmPoolId(vmPoolId).isEmpty();
    }

    private boolean removeVmPool() {
        if (getVmPoolId() != null && canRemoveVmPool(getVmPoolId())) {
            vmPoolDao.remove(getVmPoolId());
            return true;
        } else {
            if (getVmPoolId() == null) {
                log.error("Failed to remove VM Pool: VM Pool ID is null");
            } else {
                log.error("Failed to remove VM Pool '{}': there are still VMs in the Pool",
                          getVmPoolName());
            }
            return false;
        }
    }

    private CommandContext createRemoveVmStepContext(VM vm) {
        CommandContext commandCtx = null;

        try {
            Map<String, String> values = Collections.singletonMap(VdcObjectType.VM.name().toLowerCase(), vm.getName());

            Step removeVmStep = executionHandler.addSubStep(getExecutionContext(),
                    getExecutionContext().getJob().getStep(StepEnum.EXECUTING),
                    StepEnum.REMOVING_VM,
                    ExecutionMessageDirector.resolveStepMessage(StepEnum.REMOVING_VM, values));

            ExecutionContext ctx = new ExecutionContext();
            ctx.setStep(removeVmStep);
            ctx.setMonitored(true);

            Map<String, Pair<String, String>> locks = new HashMap<>();
            addVmLocks(vm, locks);
            EngineLock engineLock = new EngineLock(locks, null);

            commandCtx = cloneContext().withoutCompensationContext().withExecutionContext(ctx).withLock(engineLock);

        } catch (RuntimeException e) {
            log.error("Failed to create command context of removing VM '{}' that was in Pool '{}': {}",
                    vm.getName(),
                    getVmPoolName(),
                    e.getMessage());
            log.debug("Exception", e);
        }

        return commandCtx;
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getActionState() == CommandActionState.EXECUTE) {
            return AuditLogType.USER_REMOVE_VM_POOL_INITIATED;
        } else {
            return getSucceeded() ? AuditLogType.USER_REMOVE_VM_POOL : AuditLogType.USER_REMOVE_VM_POOL_FAILED;
        }
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.Cluster.name().toLowerCase(), getClusterName());
        }
        return jobProperties;
    }

}
