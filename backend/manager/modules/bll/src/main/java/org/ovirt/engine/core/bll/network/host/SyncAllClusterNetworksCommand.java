package org.ovirt.engine.core.bll.network.host;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.ClusterCommandBase;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ClusterParametersBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.dao.VdsDao;

@NonTransactiveCommandAttribute
public class SyncAllClusterNetworksCommand extends ClusterCommandBase<ClusterParametersBase> {

    @Inject
    private VdsDao vdsDao;
    private List<VDS> hosts;

    public SyncAllClusterNetworksCommand(ClusterParametersBase parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void init() {
        hosts = vdsDao.getAllForCluster(getParameters().getClusterId());
    }

    @Override
    protected void executeCommand() {
        List<ActionParametersBase> params = hosts.stream()
                .map(host -> new VdsActionParameters(host.getId()))
                .collect(Collectors.toList());
        runInternalMultipleActions(ActionType.SyncAllHostNetworks, params);
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.CLUSTER_SYNC_ALL_NETWORKS_STARTED : AuditLogType.CLUSTER_SYNC_ALL_NETWORKS_FAILED;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionSubjects = hosts.stream()
                .map(host -> new PermissionSubject(host.getId(), VdcObjectType.VDS, getActionType().getActionGroup()))
                .collect(Collectors.toList());
        return permissionSubjects;
    }
}
