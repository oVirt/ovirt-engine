package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.vdscommands.ActivateVdsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@LockIdNameAttribute
@NonTransactiveCommandAttribute
public class ActivateVdsCommand<T extends VdsActionParameters> extends VdsCommand<T> {
    public ActivateVdsCommand(T parameters) {
        super(parameters);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if(getParameters().isRunSilent()) {
            return getSucceeded() ? AuditLogType.VDS_ACTIVATE_ASYNC : AuditLogType.VDS_ACTIVATE_FAILED_ASYNC;
        } else {
            return getSucceeded() ? AuditLogType.VDS_ACTIVATE : AuditLogType.VDS_ACTIVATE_FAILED;
        }
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
        EngineLock monitoringLock =
                new EngineLock(Collections.singletonMap(getParameters().getVdsId().toString(),
                        LockingGroup.VDS_INIT.name()), null);
        log.infoFormat("Before acquiring lock in order to prevent monitoring for host {0} from data-center {1}",
                vds.getName(),
                vds.getstorage_pool_name());
        getLockManager().acquireLockWait(monitoringLock);
        log.infoFormat("Lock acquired, from now a monitoring of host will be skipped for host {0} from data-center {1}",
                vds.getName(),
                vds.getstorage_pool_name());
        try {
            ExecutionHandler.updateSpecificActionJobCompleted(vds.getId(), VdcActionType.MaintananceVds, false);
            TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

                @Override
                public Void runInTransaction() {
                    getCompensationContext().snapshotEntityStatus(vds.getDynamicData(), vds.getstatus());
                    runVdsCommand(VDSCommandType.SetVdsStatus,
                            new SetVdsStatusVDSCommandParameters(getVdsId(), VDSStatus.Unassigned));
                    getCompensationContext().stateChanged();
                    return null;
                }
            });

            setSucceeded(runVdsCommand(VDSCommandType.ActivateVds, new ActivateVdsVDSCommandParameters(getVdsId()))
                    .getSucceeded());
            if (getSucceeded()) {
                TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

                    @Override
                    public Void runInTransaction() {
                        // set network to operational / non-operational
                        List<Network> networks = DbFacade.getInstance().getNetworkDao()
                                .getAllForCluster(vds.getvds_group_id());
                        for (Network net : networks) {
                            NetworkClusterHelper.setStatus(vds.getvds_group_id(), net);
                        }
                        return null;
                    }
                });
            }
        } finally {
            getLockManager().releaseLock(monitoringLock);
            log.infoFormat("Activate finished. Lock released. Monitoring can run now for host {0} from data-center {1}",
                    vds.getName(),
                    vds.getstorage_pool_name());
        }
    }

    @Override
    protected boolean canDoAction() {
        if (getVds() == null) {
            return failCanDoAction(VdcBllMessages.VDS_CANNOT_ACTIVATE_VDS_NOT_EXIST);
        }
        if (getVds().getstatus() == VDSStatus.Up) {
            return failCanDoAction(VdcBllMessages.VDS_CANNOT_ACTIVATE_VDS_ALREADY_UP);
        }
        return true;
    }

    @Override
    protected Map<String, String> getExclusiveLocks() {
        return Collections.singletonMap(getParameters().getVdsId().toString(), LockingGroup.VDS.name());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ACTIVATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__HOST);
    }
}
