package org.ovirt.engine.core.bll.storage.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.ProcessOvfUpdateParameters;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

@NonTransactiveCommandAttribute(forceCompensation = false)
public class UpdateOvfStoreForStorageDomainCommand<T extends StorageDomainParametersBase> extends
        StorageDomainCommandBase<T> {

    public UpdateOvfStoreForStorageDomainCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setStorageDomainId(getParameters().getStorageDomainId());
    }

    @Override
    protected boolean validate() {
        return checkStoragePool() && checkStorageDomain();
    }

    @Override
    protected void executeCommand() {
        Guid storageDomainId = getStorageDomainId();
        ProcessOvfUpdateParameters parameters =
                new ProcessOvfUpdateParameters(getStoragePoolId(), getStorageDomainId());
        ActionReturnValue actionReturnValue =
                runInternalAction(ActionType.ProcessOvfUpdateForStoragePool, parameters, getContext());
        Set<Guid> proccessedDomains = actionReturnValue.getActionReturnValue();

        if (actionReturnValue.getSucceeded() && proccessedDomains != null &&
                proccessedDomains.contains(storageDomainId)) {
            runInternalActionWithTasksContext(ActionType.ProcessOvfUpdateForStorageDomain,
                    createProcessOvfUpdateForDomainParams());
        } else {
            log.info("OVFs update was ignored - nothing to update for storage domain '{}'", storageDomainId);
        }
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
            if (getStoragePoolId() != null) {
                locks.put(getStoragePoolId().toString(),
                        LockMessagesMatchUtil.makeLockingPair(LockingGroup.OVF_UPDATE,
                                EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
            }
            return locks;
        }
        return null;
    }
}
