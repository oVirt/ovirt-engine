package org.ovirt.engine.core.bll.network.host;

import static org.ovirt.engine.core.common.AuditLogType.CLUSTER_SYNC_ALL_NETWORKS_FAILED;
import static org.ovirt.engine.core.common.AuditLogType.CLUSTER_SYNC_ALL_NETWORKS_FINISHED;
import static org.ovirt.engine.core.common.AuditLogType.CLUSTER_SYNC_ALL_NETWORKS_STARTED;
import static org.ovirt.engine.core.common.AuditLogType.CLUSTER_SYNC_ALL_NETWORKS_START_ERROR;
import static org.ovirt.engine.core.common.AuditLogType.CLUSTER_SYNC_NOTHING_TO_DO;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.bll.ClusterCommandBase;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ClusterParametersBase;
import org.ovirt.engine.core.common.action.PersistentHostSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;

@NonTransactiveCommandAttribute
public class SyncAllClusterNetworksCommand extends ClusterCommandBase<ClusterParametersBase> {

    @Inject
    private ClusterDao clusterDao;
    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;
    private Set<Guid> outOfSyncVdsIds;

    public SyncAllClusterNetworksCommand(ClusterParametersBase parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void init() {
        QueryReturnValue qRetVal = runInternalQuery(
            QueryType.GetOutOfSyncHostsForCluster, new IdQueryParameters(getParameters().getClusterId()));
        outOfSyncVdsIds = qRetVal.getReturnValue();
    }

    @Override
    protected void executeCommand() {
        if (outOfSyncHostsExist()) {
            AtomicInteger count = new AtomicInteger(1);
            List<ActionParametersBase> params = outOfSyncVdsIds.stream()
                .map(vdsId -> new PersistentHostSetupNetworksParameters(
                    vdsId, outOfSyncVdsIds.size(), count.getAndIncrement())
                )
                .collect(Collectors.toList());
            params.forEach(this::withRootCommandInfo);
            runInternalMultipleActions(ActionType.SyncAllHostNetworks, params);
        }
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
            case EXECUTE:
                if (!getSucceeded()){
                    return CLUSTER_SYNC_ALL_NETWORKS_START_ERROR;
                } else if (!outOfSyncHostsExist()){
                    return CLUSTER_SYNC_NOTHING_TO_DO;
                } else {
                    return CLUSTER_SYNC_ALL_NETWORKS_STARTED;
                }
            case END_SUCCESS:
                return CLUSTER_SYNC_ALL_NETWORKS_FINISHED;
        }
        return CLUSTER_SYNC_ALL_NETWORKS_FAILED;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return outOfSyncVdsIds.stream()
            .map(vdsId -> new PermissionSubject(vdsId, VdcObjectType.VDS, getActionType().getActionGroup()))
            .collect(Collectors.toList());
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
        }
        Cluster cluster = clusterDao.get(getParameters().getClusterId());
        jobProperties.put(VdcObjectType.Cluster.name().toLowerCase(), cluster.getName());
        return jobProperties;
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
        return CollectionUtils.isNotEmpty(outOfSyncVdsIds);
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }
}
