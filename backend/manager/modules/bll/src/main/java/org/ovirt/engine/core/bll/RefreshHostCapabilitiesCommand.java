package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

@NonTransactiveCommandAttribute
public class RefreshHostCapabilitiesCommand<T extends VdsActionParameters> extends VdsCommand<T> {

    public RefreshHostCapabilitiesCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected void executeCommand() {
        try (EngineLock monitoringLock = acquireMonitorLock()) {
            ResourceManager.getInstance().GetVdsManager(getVdsId()).refreshHost(getVds());
            setSucceeded(true);
        }

        logMonitorLockReleased("Refresh host capabilities");
    }

    @Override
    protected boolean canDoAction() {
        return validate(hostExists()) && validate(hostStatusValid());
    }

    private ValidationResult hostExists() {
        return getVds() == null ? new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_HOST_NOT_EXIST) : ValidationResult.VALID;
    }

    private ValidationResult hostStatusValid() {
        VDSStatus hostStatus = getVds().getStatus();
        if (hostStatus != VDSStatus.Maintenance && hostStatus != VDSStatus.Up && hostStatus != VDSStatus.NonOperational) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL,
                    VdcBllMessages.VAR__HOST_STATUS__UP_MAINTENANCE_OR_NON_OPERATIONAL.name());
        }

        return ValidationResult.VALID;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getParameters().getVdsId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VDS, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REFRESH);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__HOST_CAPABILITIES);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.HOST_REFRESHED_CAPABILITIES
                : AuditLogType.HOST_REFRESH_CAPABILITIES_FAILED;
    }
}
