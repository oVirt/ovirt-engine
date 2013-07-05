package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.ActivateVdsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@LockIdNameAttribute
@NonTransactiveCommandAttribute
public class ActivateVdsCommand<T extends VdsActionParameters> extends VdsCommand<T> {

    public ActivateVdsCommand(T parameters) {
        super(parameters);
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected ActivateVdsCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {

        final VDS vds = getVds();
        try (EngineLock monitoringLock = acquireMonitorLock()) {
            ExecutionHandler.updateSpecificActionJobCompleted(vds.getId(), VdcActionType.MaintenanceVds, false);
            runVdsCommand(VDSCommandType.SetVdsStatus,
                    new SetVdsStatusVDSCommandParameters(getVdsId(), VDSStatus.Unassigned));

            VDSReturnValue returnValue =
                    runVdsCommand(VDSCommandType.ActivateVds, new ActivateVdsVDSCommandParameters(getVdsId()));
            setSucceeded(returnValue.getSucceeded());

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
            }
        }

        logMonitorLockReleased("Activate");
    }

    @Override
    protected boolean canDoAction() {
        if (getVds() == null) {
            return failCanDoAction(VdcBllMessages.VDS_CANNOT_ACTIVATE_VDS_NOT_EXIST);
        }

        if (StringUtils.isBlank(getVds().getUniqueId()) && Config.<Boolean> GetValue(ConfigValues.InstallVds)) {
            return failCanDoAction(VdcBllMessages.VDS_CANNOT_ACTIVATE_VDS_NO_UUID);
        }

        if (getVds().getStatus() == VDSStatus.Up) {
            return failCanDoAction(VdcBllMessages.VDS_CANNOT_ACTIVATE_VDS_ALREADY_UP);
        }
        return true;
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
            return getSucceeded() ? AuditLogType.VDS_ACTIVATE_ASYNC : AuditLogType.VDS_ACTIVATE_FAILED_ASYNC;
        } else {
            return getSucceeded() ? AuditLogType.VDS_ACTIVATE : AuditLogType.VDS_ACTIVATE_FAILED;
        }
    }
}
