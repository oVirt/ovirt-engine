package org.ovirt.engine.core.bll.network.host;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.host.SetupNetworksCommand.SETUP_NETWORKS_RESOLUTION;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.PersistentSetupNetworksParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;

@NonTransactiveCommandAttribute
public class PersistentSetupNetworksCommand<T extends PersistentSetupNetworksParameters> extends VdsCommand<T> {

    public PersistentSetupNetworksCommand(T parameters) {
        this(parameters, null);
    }

    public PersistentSetupNetworksCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setVdsId(parameters.getVdsId());
    }


    public int getSequence() {
        return getParameters().getSequence();
    }

    public int getTotal() {
        return getParameters().getTotal();
    }

    public String getNetworkNames() {
        return getParameters().getNetworkNames();
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__SETUP);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__NETWORKS);
    }

    @Override
    protected void executeCommand() {
        if (getParameters().getShouldBeLogged()) {
            AuditLogDirector.log(this, AuditLogType.PERSIST_NETWORK_ON_HOST);
        }

        VdcReturnValueBase returnValue = getBackend().runInternalAction(VdcActionType.SetupNetworks, getParameters());
        if (returnValue.getSucceeded()
                && SETUP_NETWORKS_RESOLUTION.NO_CHANGES_DETECTED != returnValue.getActionReturnValue()) {
            VdsActionParameters parameters = new VdsActionParameters(getParameters().getVdsId());
            parameters.setShouldBeLogged(false);
            parameters.setCorrelationId(getCorrelationId());
            returnValue =
                    getBackend().runInternalAction(VdcActionType.CommitNetworkChanges,
                            parameters);
        }

        if (!returnValue.getSucceeded()) {
            propagateFailure(returnValue);
        }

        setSucceeded(returnValue.getSucceeded());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.PERSIST_NETWORK_ON_HOST_FINISHED
                : AuditLogType.PERSIST_NETWORK_ON_HOST_FAILED;
    }
}
