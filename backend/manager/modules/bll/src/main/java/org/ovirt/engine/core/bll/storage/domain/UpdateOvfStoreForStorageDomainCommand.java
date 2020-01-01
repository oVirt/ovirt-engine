package org.ovirt.engine.core.bll.storage.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.ProcessOvfUpdateParameters;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;


@NonTransactiveCommandAttribute
public class UpdateOvfStoreForStorageDomainCommand<T extends StorageDomainParametersBase> extends
        StorageDomainCommandBase<T> implements SerialChildExecutingCommand {
    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;

    public UpdateOvfStoreForStorageDomainCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setStorageDomainId(getParameters().getStorageDomainId());
    }

    @Override
    protected boolean validate() {
        return validate(createStoragePoolValidator().exists()) && checkStorageDomain();
    }

    @Override
    protected void executeCommand() {
        ProcessOvfUpdateParameters parameters =
                new ProcessOvfUpdateParameters(getStoragePoolId(), getStorageDomainId());
        ActionReturnValue actionReturnValue =
                runInternalAction(ActionType.ProcessOvfUpdateForStoragePool, parameters, cloneContext().withoutLock());
        if (!actionReturnValue.getSucceeded()) {
            propagateFailure(actionReturnValue);
            setCommandStatus(CommandStatus.FAILED);
            setSucceeded(true);
            return;
        }
        getReturnValue().setActionReturnValue(actionReturnValue.getActionReturnValue());
        setSucceeded(true);
    }

    private ProcessOvfUpdateParameters createProcessOvfUpdateForDomainParams() {
        ProcessOvfUpdateParameters params =
                new ProcessOvfUpdateParameters(getStoragePoolId(), getStorageDomainId());
        params.setSkipDomainChecks(true);
        if (isExecutedAsChildCommand()) {
            params.setParentCommand(getParameters().getParentCommand());
            params.setEntityInfo(getParameters().getParentParameters().getEntityInfo());
            params.setParentParameters(getParameters().getParentParameters());
        }
        return params;
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Command);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (getStorageDomain() != null) {
            Map<String, Pair<String, String>> locks = new HashMap<>();
            locks.put(getStorageDomainId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE,
                            EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
            return locks;
        }
        return null;
    }

    public boolean performNextOperation(int completedChildCount) {
        if (StorageDomainParametersBase.Phase.PROCESS_OVF_UPDATE_FOR_STORAGE_DOMAIN == getParameters().getPhase()) {
            return false;
        }
        Guid storageDomainId = getStorageDomainId();
        Set<Guid> proccessedDomains = getReturnValue().getActionReturnValue();
        if (proccessedDomains != null && proccessedDomains.contains(storageDomainId)) {
            getParameters().setPhase(StorageDomainParametersBase.Phase.PROCESS_OVF_UPDATE_FOR_STORAGE_DOMAIN);
        } else {
            addCustomValue("StorageDomainName", getStorageDomain().getName());
            auditLogDirector.log(this, AuditLogType.OVF_STORES_UPDATE_IGNORED);
            setCommandStatus(CommandStatus.SUCCEEDED);
            setSucceeded(true);
            return false;
        }
        persistCommandIfNeeded();
        executeNextOperation();
        return true;
    }

    private void executeNextOperation() {
        if (getParameters().getPhase() == StorageDomainParametersBase.Phase.PROCESS_OVF_UPDATE_FOR_STORAGE_DOMAIN) {

            ActionReturnValue returnValue = runInternalActionWithTasksContext(ActionType.ProcessOvfUpdateForStorageDomain,
                    createProcessOvfUpdateForDomainParams());
            if (!returnValue.getSucceeded()) {
                propagateFailure(returnValue);
                setCommandStatus(CommandStatus.FAILED);
                throw new EngineException(returnValue.getFault().getError(), returnValue.getFault().getMessage());
            }
            setCommandStatus(CommandStatus.SUCCEEDED);
            setSucceeded(true);
        }
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }
}
