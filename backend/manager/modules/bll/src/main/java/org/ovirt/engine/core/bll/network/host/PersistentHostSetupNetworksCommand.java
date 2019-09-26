package org.ovirt.engine.core.bll.network.host;

import static org.ovirt.engine.core.common.FeatureSupported.isSkipCommitNetworkChangesSupported;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.HostSetupNetworksParameters;
import org.ovirt.engine.core.common.action.PersistentHostSetupNetworksParameters;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;

@NonTransactiveCommandAttribute
public class PersistentHostSetupNetworksCommand<T extends PersistentHostSetupNetworksParameters> extends VdsCommand<T> {

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private VdsDynamicDao vdsDynamicDao;

    @Inject
    private VdsDao vdsDao;

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
        addValidationMessage(EngineMessage.VAR__ACTION__SETUP);
        addValidationMessage(EngineMessage.VAR__TYPE__NETWORKS);
    }

    @Override
    protected void executeCommand() {
        if (getParameters().getShouldBeLogged()) {
            auditLogDirector.log(this, AuditLogType.PERSIST_HOST_SETUP_NETWORK_ON_HOST);
        }
        HostSetupNetworksParameters params = new HostSetupNetworksParameters(getParameters());
        params.setCorrelationId(getParameters().getCorrelationId());
        ActionReturnValue returnValue =
                runInternalAction(ActionType.HostSetupNetworks, params, cloneContextAndDetachFromParent());

        boolean skipCommit = params.isCommitOnSuccess() && isSkipCommitNetworkChangesSupported(vdsDao.get(getVdsId()));
        if (returnValue.getSucceeded() && !skipCommit) {
            boolean changesDetected = checkForChanges();
            if (changesDetected) {
                VdsActionParameters parameters = new VdsActionParameters(getParameters().getVdsId());
                parameters.setShouldBeLogged(false);
                parameters.setCorrelationId(getCorrelationId());
                returnValue = runInternalAction(ActionType.CommitNetworkChanges,
                        parameters, cloneContextAndDetachFromParent());
            }
        }

        if (!returnValue.getSucceeded()) {
            propagateFailure(returnValue);
        }

        setSucceeded(returnValue.getSucceeded());
    }

    private boolean checkForChanges() {
        final VdsDynamic host = vdsDynamicDao.get(getVdsId());
        final Boolean netConfigDirty = host.getNetConfigDirty();
        return Boolean.TRUE.equals(netConfigDirty);
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
