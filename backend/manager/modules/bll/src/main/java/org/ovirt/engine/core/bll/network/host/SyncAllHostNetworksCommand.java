package org.ovirt.engine.core.bll.network.host;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.predicate.NetworkNotInSyncPredicate;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.HostSetupNetworksParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.utils.linq.LinqUtils;

public class SyncAllHostNetworksCommand extends VdsCommand {

    @Inject
    private NetworkAttachmentDao networkAttachmentDao;

    public SyncAllHostNetworksCommand(VdsActionParameters parameters) {
        super(parameters);
    }

    public SyncAllHostNetworksCommand(VdsActionParameters parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void executeCommand() {
        HostSetupNetworksParameters parameters = generateSyncAllHostNetworksParameters();
        VdcReturnValueBase retVal = runInternalAction(VdcActionType.PersistentHostSetupNetworks,
                parameters,
                cloneContextAndDetachFromParent());
        if (!retVal.getSucceeded()) {
            propagateFailure(retVal);
        } else {
            setSucceeded(true);
        }

    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.HOST_SYNC_ALL_NETWORKS_FINISHED
                : AuditLogType.HOST_SYNC_ALL_NETWORKS_FAILED;
    }

    private HostSetupNetworksParameters generateSyncAllHostNetworksParameters() {
        HostSetupNetworksParameters parameters = new HostSetupNetworksParameters(getVdsId());
        VdcQueryReturnValue returnValue = runInternalQuery(VdcQueryType.GetNetworkAttachmentsByHostId,
                new IdQueryParameters(getVdsId()));
        List<NetworkAttachment> networkAttachments = returnValue.getReturnValue();
        List<NetworkAttachment> unSyncNetworkAttachments =
                LinqUtils.filter(networkAttachments, new NetworkNotInSyncPredicate());
        for (NetworkAttachment networkAttachment : unSyncNetworkAttachments) {
            networkAttachment.setOverrideConfiguration(true);
        }
        parameters.setNetworkAttachments(unSyncNetworkAttachments);
        return parameters;
    }
}
