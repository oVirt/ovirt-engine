package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

@NonTransactiveCommandAttribute
public class RefreshHostCapabilitiesCommand<T extends VdsActionParameters> extends RefreshHostInfoCommandBase<T> {

    @Inject
    private ResourceManager resourceManager;

    @Inject
    private HostLocking hostLocking;

    public RefreshHostCapabilitiesCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected void executeCommand() {
        try (EngineLock monitoringLock = acquireMonitorLock("Refresh host capabilities")) {
            resourceManager.getVdsManager(getVdsId()).refreshHostSync(getVds());
            setSucceeded(true);
        }
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Guid hostId = getParameters().getVdsId();

        Map<String, Pair<String, String>> exclusiveLocks = new HashMap<>();

        exclusiveLocks.put(hostId.toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VDS,
                        EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        exclusiveLocks.putAll(hostLocking.getSetupNetworksLock(hostId));
        return exclusiveLocks;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REFRESH);
        addValidationMessage(EngineMessage.VAR__TYPE__HOST_CAPABILITIES);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.HOST_REFRESHED_CAPABILITIES
                : AuditLogType.HOST_REFRESH_CAPABILITIES_FAILED;
    }
}
