package org.ovirt.engine.core.bll.network.host;

import static org.ovirt.engine.core.common.AuditLogType.CLUSTER_SYNC_ALL_NETWORKS_FAILED;
import static org.ovirt.engine.core.common.AuditLogType.CLUSTER_SYNC_ALL_NETWORKS_STARTED;
import static org.ovirt.engine.core.common.AuditLogType.CLUSTER_SYNC_NOTHING_TO_DO;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.bll.ClusterCommandBase;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ClusterParametersBase;
import org.ovirt.engine.core.common.action.PersistentHostSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;

@NonTransactiveCommandAttribute
public class SyncAllClusterNetworksCommand extends ClusterCommandBase<ClusterParametersBase> {

    private List<VDS> outOfSyncHosts;

    public SyncAllClusterNetworksCommand(ClusterParametersBase parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void init() {
        QueryReturnValue qRetVal = runInternalQuery(
            QueryType.GetOutOfSyncHostsForCluster, new IdQueryParameters(getParameters().getClusterId()));
        outOfSyncHosts = qRetVal.getReturnValue();
    }

    @Override
    protected void executeCommand() {
        if (outOfSyncHostsExist()) {
            AtomicInteger count = new AtomicInteger(1);
            List<ActionParametersBase> params = outOfSyncHosts.stream()
                .map(host -> new PersistentHostSetupNetworksParameters(host.getId(), outOfSyncHosts.size(), count.getAndIncrement()))
                .collect(Collectors.toList());
            runInternalMultipleActions(ActionType.SyncAllHostNetworks, params);
        }
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()){
            return outOfSyncHostsExist() ? CLUSTER_SYNC_ALL_NETWORKS_STARTED : CLUSTER_SYNC_NOTHING_TO_DO;
        }
        return CLUSTER_SYNC_ALL_NETWORKS_FAILED;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return outOfSyncHosts.stream()
            .map(host -> new PermissionSubject(host.getId(), VdcObjectType.VDS, getActionType().getActionGroup()))
            .collect(Collectors.toList());
    }

    /**
     * When there are no eligible hosts for a sync-networks operation there are no
     * permission subjects so return false to allow this command to complete without
     * generating a 'user is not authorized' error response
     */
    @Override
    protected boolean objectsRequiringPermissionExist() {
        return outOfSyncHostsExist();
    }

    private boolean outOfSyncHostsExist() {
        return CollectionUtils.isNotEmpty(outOfSyncHosts);
    }
}
