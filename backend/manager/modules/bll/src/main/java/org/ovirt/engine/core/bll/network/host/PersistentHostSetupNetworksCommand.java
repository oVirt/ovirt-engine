package org.ovirt.engine.core.bll.network.host;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.host.function.NetworkNameFromNetworkAttachmentTransformationFunction;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.PersistentHostSetupNetworksParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.utils.linq.LinqUtils;

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
        if (getParameters().getNetworkAttachments().size() == 0) {
            getParameters().setNetworkNames(StringUtils.EMPTY);
        }
        else if (StringUtils.isEmpty(getParameters().getNetworkNames())) {
            updateModifiedNetworksNames();
        }
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

    private void updateModifiedNetworksNames() {
        Collection<String> networkNames =
                LinqUtils.transformToList(getParameters().getNetworkAttachments(),
                        new NetworkNameFromNetworkAttachmentTransformationFunction());
        getParameters().setNetworkNames(StringUtils.join(networkNames, ", "));
    }

    private boolean checkForChanges() {
        boolean output = getVdsDynamicDao().get(getVdsId()).getNetConfigDirty();
        return output;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.PERSIST_NETWORK_ON_HOST_FINISHED
                : AuditLogType.PERSIST_NETWORK_ON_HOST_FAILED;
    }
}
