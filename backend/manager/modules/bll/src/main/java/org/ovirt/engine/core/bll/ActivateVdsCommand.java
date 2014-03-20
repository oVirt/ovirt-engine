package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.HaMaintenanceMode;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.SetHaMaintenanceModeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class ActivateVdsCommand<T extends VdsActionParameters> extends VdsCommand<T> {

    private boolean haMaintenanceFailed;

    public ActivateVdsCommand(T parameters) {
        super(parameters);
        haMaintenanceFailed = false;
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected ActivateVdsCommand(Guid commandId) {
        super(commandId);
        haMaintenanceFailed = false;
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected void executeCommand() {

        final VDS vds = getVds();
        try (EngineLock monitoringLock = acquireMonitorLock()) {
            ExecutionHandler.updateSpecificActionJobCompleted(vds.getId(), VdcActionType.MaintenanceVds, false);
            setSucceeded(runVdsCommand(VDSCommandType.SetVdsStatus,
                    new SetVdsStatusVDSCommandParameters(getVdsId(), VDSStatus.Unassigned)).getSucceeded()
            );

            if (getSucceeded()) {
                TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

                    @Override
                    public Void runInTransaction() {
                        // set network to operational / non-operational
                        List<Network> networks = getNetworkDAO().getAllForCluster(vds.getVdsGroupId());
                        for (Network net : networks) {
                            NetworkClusterHelper.setStatus(vds.getVdsGroupId(), net);
                        }
                        return null;
                    }
                });

                if (vds.getHighlyAvailableIsConfigured()) {
                    SetHaMaintenanceModeVDSCommandParameters param
                            = new SetHaMaintenanceModeVDSCommandParameters(vds, HaMaintenanceMode.LOCAL, false);
                    if (!runVdsCommand(VDSCommandType.SetHaMaintenanceMode, param).getSucceeded()) {
                        haMaintenanceFailed = true;
                    }
                }
            }
        }

        logMonitorLockReleased("Activate");
    }

    @Override
    protected boolean canDoAction() {
        VdsValidator validator = new VdsValidator(getVds());
        return validate(validator.exists()) &&
                validate(validator.validateStatusForActivation()) &&
                validate(validator.validateUniqueId());
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getParameters().getVdsId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VDS, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ACTIVATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__HOST);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getParameters().isRunSilent()) {
            return getSucceeded()
                    ? (haMaintenanceFailed
                            ? AuditLogType.VDS_ACTIVATE_MANUAL_HA_ASYNC
                            : AuditLogType.VDS_ACTIVATE_ASYNC)
                    : AuditLogType.VDS_ACTIVATE_FAILED_ASYNC;
        } else {
            return getSucceeded()
                    ? (haMaintenanceFailed
                            ? AuditLogType.VDS_ACTIVATE_MANUAL_HA
                            : AuditLogType.VDS_ACTIVATE)
                    : AuditLogType.VDS_ACTIVATE_FAILED;
        }
    }
}
