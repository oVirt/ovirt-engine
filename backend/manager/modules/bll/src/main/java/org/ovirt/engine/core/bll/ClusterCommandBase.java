package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.ClusterValidator;
import org.ovirt.engine.core.common.action.ClusterParametersBase;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.ClusterFeatureDao;
import org.ovirt.engine.core.dao.LabelDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.SupportedHostFeatureDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

public abstract class ClusterCommandBase<T extends ClusterParametersBase> extends CommandBase<T> {

    @Inject
    private ClusterPermissionsFinder clusterPermissionsFinder;
    @Inject
    protected CpuFlagsManagerHandler cpuFlagsManagerHandler;
    @Inject
    private ClusterDao clusterDao;
    @Inject
    private StoragePoolDao storagePoolDao;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private GlusterVolumeDao glusterVolumeDao;
    @Inject
    private ClusterFeatureDao clusterFeatureDao;
    @Inject
    private SupportedHostFeatureDao hostFeatureDao;
    @Inject
    private LabelDao labelDao;
    @Inject
    private NetworkDao networkDao;

    private Cluster cluster;

    protected ClusterCommandBase(Guid commandId) {
        super(commandId);
    }

    public ClusterCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setClusterId(parameters.getClusterId());
    }

    protected ClusterValidator getClusterValidator(Cluster cluster) {
        return new ClusterValidator(clusterDao, storagePoolDao, cluster, cpuFlagsManagerHandler);
    }

    protected ClusterValidator getClusterValidator(Cluster cluster, Cluster newCluster) {
        return new ClusterValidator(clusterDao,
                storagePoolDao,
                cluster,
                cpuFlagsManagerHandler,
                newCluster,
                vdsDao,
                vmDao,
                glusterVolumeDao,
                clusterFeatureDao,
                hostFeatureDao,
                labelDao,
                networkDao);
    }

    @Override
    public Cluster getCluster() {
        if (cluster == null) {
            cluster = clusterDao.get(getParameters().getClusterId());
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
