package org.ovirt.engine.core.bll.network.host;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.PersistentHostSetupNetworksParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;

@NonTransactiveCommandAttribute
public class PersistentHostSetupNetworksCommand<T extends PersistentHostSetupNetworksParameters> extends VdsCommand<T> {

    public PersistentHostSetupNetworksCommand(T parameters) {
        this(parameters, null);
    }

    public PersistentHostSetupNetworksCommand(T parameters, CommandContext commandContext) {
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
        addCanDoActionMessage(EngineMessage.VAR__ACTION__SETUP);
        addCanDoActionMessage(EngineMessage.VAR__TYPE__NETWORKS);
    }

    @Override
    protected void executeCommand() {
        if (getParameters().getShouldBeLogged()) {
            auditLogDirector.log(this, AuditLogType.PERSIST_HOST_SETUP_NETWORK_ON_HOST);
        }

        VdcReturnValueBase returnValue =
                runInternalAction(VdcActionType.HostSetupNetworks, getParameters(), cloneContextAndDetachFromParent());
        boolean changesDetected = checkForChanges();
        if (returnValue.getSucceeded() && changesDetected) {
            VdsActionParameters parameters = new VdsActionParameters(getParameters().getVdsId());
            parameters.setShouldBeLogged(false);
            parameters.setCorrelationId(getCorrelationId());
            returnValue = runInternalAction(VdcActionType.CommitNetworkChanges,
                    parameters, cloneContextAndDetachFromParent());
        }

        if (!returnValue.getSucceeded()) {
            propagateFailure(returnValue);
        }

        setSucceeded(returnValue.getSucceeded());
    }

    private boolean checkForChanges() {
        boolean output = getVdsDynamicDao().get(getVdsId()).getNetConfigDirty();
        return output;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (StringUtils.isEmpty(getParameters().getNetworkNames())) {
            return getSucceeded() ? AuditLogType.PERSIST_SETUP_NETWORK_ON_HOST_FINISHED
                    : AuditLogType.PERSIST_SETUP_NETWORK_ON_HOST_FAILED;
        } else {
            return getSucceeded() ? AuditLogType.PERSIST_NETWORK_ON_HOST_FINISHED
                    : AuditLogType.PERSIST_NETWORK_ON_HOST_FAILED;
        }
    }
}
