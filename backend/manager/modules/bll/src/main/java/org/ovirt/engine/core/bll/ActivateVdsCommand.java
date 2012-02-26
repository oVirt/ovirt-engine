package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.vdscommands.ActivateVdsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public class ActivateVdsCommand<T extends VdsActionParameters> extends VdsCommand<T> {
    public ActivateVdsCommand(T parameters) {
        super(parameters);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.VDS_ACTIVATE : AuditLogType.VDS_ACTIVATE_FAILED;
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

        // VDS vds = ResourceManager.Instance.getVds(VdsId);
        if (getVds() == null) {
            setSucceeded(false);
        } else {
            ExecutionHandler.updateSpecificActionJobCompleted(getVds().getId(), VdcActionType.MaintananceVds, false);
            TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

                @Override
                public Void runInTransaction() {
                    getCompensationContext().snapshotEntityStatus(getVds().getDynamicData(), getVds().getstatus());
                    Backend.getInstance().getResourceManager().RunVdsCommand(VDSCommandType.SetVdsStatus,
                            new SetVdsStatusVDSCommandParameters(getVdsId(), VDSStatus.Unassigned));
                    getCompensationContext().stateChanged();
                    return null;
                }
            });

            setSucceeded(Backend.getInstance().getResourceManager()
                    .RunVdsCommand(VDSCommandType.ActivateVds, new ActivateVdsVDSCommandParameters(getVdsId()))
                    .getSucceeded());
            if (getSucceeded()) {
                // set network to operational / non-operational
                List<network> networks = DbFacade.getInstance().getNetworkDAO()
                        .getAllForCluster(getVds().getvds_group_id());
                for (network net : networks) {
                    AttachNetworkToVdsGroupCommand.SetNetworkStatus(getVds().getvds_group_id(), net);
                }
            }
        }
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = true;
        if (getVds() == null) {
            addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_ACTIVATE_VDS_NOT_EXIST);
            returnValue = false;
        }
        if (getVds().getstatus() == VDSStatus.Up) {
            addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_ACTIVATE_VDS_ALREADY_UP);
            returnValue = false;
        }
        return returnValue;
    }
}
