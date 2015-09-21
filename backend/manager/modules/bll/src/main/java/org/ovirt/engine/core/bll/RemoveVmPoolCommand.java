package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.RemoveVmFromPoolParameters;
import org.ovirt.engine.core.common.action.RemoveVmParameters;
import org.ovirt.engine.core.common.action.StopVmParameters;
import org.ovirt.engine.core.common.action.StopVmTypeEnum;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
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
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonTransactiveCommandAttribute
public class RemoveVmPoolCommand<T extends VmPoolParametersBase> extends VmPoolCommandBase<T> {

    private static final Logger log = LoggerFactory.getLogger(RemoveVmPoolCommand.class);

    private List<VM> cachedVmsInPool;

    public RemoveVmPoolCommand(T parameters) {
        this(parameters, null);
    }

    public RemoveVmPoolCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        // set group id for logging and job
        if (getVmPool() != null) {
            setVdsGroupId(getVmPool().getVdsGroupId());
        }
    }

    private List<VM> getCachedVmsInPool() {
        if (cachedVmsInPool == null && getVmPoolId() != null) {
            cachedVmsInPool = DbFacade.getInstance().getVmDao().getAllForVmPool(getVmPoolId());
        }
        return cachedVmsInPool;
    }

    @Override
    protected boolean canDoAction() {
        if (getVmPool() == null) {
            return failCanDoAction(EngineMessage.VM_POOL_NOT_FOUND);
        }
        return true;
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Execution);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Map<String, Pair<String, String>> locks = new HashMap<>();
        locks.put(getVmPoolId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_POOL, getVmPoolIsBeingRemovedMessage()));
        for (VM vm : getCachedVmsInPool()) {
            addVmLocks(vm, locks);
        }
        return locks;
    }

    private void addVmLocks(VM vm, Map<String, Pair<String, String>> locks) {
        locks.put(vm.getId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, getVmIsBeingRemovedMessage(vm)));
    }

    private String getVmPoolIsBeingRemovedMessage() {
        StringBuilder builder = new StringBuilder(
                EngineMessage.ACTION_TYPE_FAILED_VM_POOL_IS_BEING_REMOVED.name());
        if (getVmPool() != null) {
            builder.append(String.format("$VmPoolName %1$s", getVmPool().getName()));
        }
        return builder.toString();
    }

    private String getVmIsBeingRemovedMessage(VM vm) {
        StringBuilder builder = new StringBuilder(
                EngineMessage.ACTION_TYPE_FAILED_VM_POOL_IS_BEING_REMOVED_WITH_VM.name());
        if (getVmPool() != null) {
            builder.append(String.format("$VmPoolName %1$s $VmName %2$s", getVmPool().getName(), vm.getName()));
        }
        return builder.toString();
    }

    @Override
    protected void executeCommand() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                getCompensationContext().snapshotEntity(getVmPool());
                setPoolBeingDestroyed();
                setPrestartedToZero();
                getCompensationContext().stateChanged();
                return null;
            }

        });
        setSucceeded(stopVms() && removeVmsInPool() && removeVmPool());
    }

    private void setPoolBeingDestroyed() {
        getVmPoolDao().setBeingDestroyed(getVmPoolId(), true);
    }

    private void setPrestartedToZero() {
        VmPool vmPool = getVmPool();
        if (vmPool.getPrestartedVms() > 0) {
            vmPool.setPrestartedVms(0);
            getVmPoolDao().update(vmPool);
        }
    }

    private boolean stopVms() {
        for (VM vm : getCachedVmsInPool()) {
            if (!vm.isDown()) {
                VdcReturnValueBase result = runInternalAction(
                        VdcActionType.StopVm,
                        withRootCommandInfo(new StopVmParameters(vm.getId(), StopVmTypeEnum.NORMAL), getActionType()),
                        cloneContextAndDetachFromParent());
                if (!result.getSucceeded()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean removeVmsInPool() {
        for (VM vm : getCachedVmsInPool()) {
            RemoveVmFromPoolParameters removeVmFromPoolParameters = new RemoveVmFromPoolParameters(vm.getId());
            removeVmFromPoolParameters.setTransactionScopeOption(TransactionScopeOption.Suppress);
            VdcReturnValueBase result = runInternalActionWithTasksContext(
                    VdcActionType.RemoveVmFromPool,
                    removeVmFromPoolParameters);
            if (!result.getSucceeded()) {
                return false;
            }

            result = runInternalAction(
                    VdcActionType.RemoveVm,
                    new RemoveVmParameters(vm.getId(), false),
                    createRemoveVmStepContext(vm));
            if (!result.getSucceeded()) {
                return false;
            } else {
                getTaskIdList().addAll(result.getInternalVdsmTaskIdList());
            }
        }

        return true;
    }

    private static boolean canRemoveVmPool(Guid vmPoolId) {
        return getListOfVmsInPool(vmPoolId).size() == 0;
    }

    private boolean removeVmPool() {
        if (getVmPoolId() != null && canRemoveVmPool(getVmPoolId())) {
            getVmPoolDao().remove(getVmPoolId());
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

            Step removeVmStep = ExecutionHandler.addSubStep(getExecutionContext(),
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
            log.error("Failed to create command context of removing VM '{}' from Pool '{}': {}",
                    vm.getName(),
                    getVmPoolName(),
                    e.getMessage());
            log.debug("Exception", e);
        }

        return commandCtx;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_VM_POOL : AuditLogType.USER_REMOVE_VM_POOL_FAILED;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.VdsGroups.name().toLowerCase(), getVdsGroupName());
        }
        return jobProperties;
    }

}
