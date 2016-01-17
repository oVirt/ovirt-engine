package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.ClusterParametersBase;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.compat.Guid;

public abstract class ClusterCommandBase<T extends ClusterParametersBase> extends CommandBase<T> {

    @Inject
    private ClusterPermissionsFinder clusterPermissionsFinder;

    @Inject
    protected CpuFlagsManagerHandler cpuFlagsManagerHandler;

    private Cluster cluster;

    protected ClusterCommandBase(Guid commandId) {
        super(commandId);
    }

    public ClusterCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setClusterId(parameters.getClusterId());
    }

    protected CpuFlagsManagerHandler getCpuFlagsManagerHandler() {
        return cpuFlagsManagerHandler;
    }

    @Override
    public Cluster getCluster() {
        if (cluster == null) {
            cluster = getClusterDao().get(getParameters().getClusterId());
        }
        return cluster;
    }

    @Override
    public String getClusterName() {
        if (getCluster() != null) {
            return getCluster().getName();
        } else {
            return null;
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return clusterPermissionsFinder.findPermissionCheckSubjects(getClusterId(), getActionType());
    }
}
