package org.ovirt.engine.core.bll.network.cluster;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

public class UpdateNetworkClusterValidator extends NetworkClusterValidatorBase {

    private final NetworkCluster oldNetworkCluster;

    public UpdateNetworkClusterValidator(InterfaceDao interfaceDao,
            NetworkDao networkDao,
            NetworkCluster networkCluster,
            NetworkCluster oldNetworkCluster) {
        super(interfaceDao, networkDao, networkCluster);

        this.oldNetworkCluster = oldNetworkCluster;
    }

    @Override
    protected boolean isManagementNetworkChanged() {
        return !oldNetworkCluster.isManagement() && networkCluster.isManagement();
    }

    public ValidationResult managementNetworkUnset() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_MANAGEMENT_NETWORK_UNSET).
                when(oldNetworkCluster.isManagement() && !networkCluster.isManagement());
    }

    public ValidationResult glusterNetworkInUseAndUnset(Cluster cluster) {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_NETWORK_INUSE).
                when(cluster.supportsGlusterService() && oldNetworkCluster.isGluster() && !networkCluster.isGluster()
                        && isGlusterNetworkInUse());
    }

    private boolean isGlusterNetworkInUse() {
        return !getGlusterBrickDao().getAllByClusterAndNetworkId(oldNetworkCluster.getClusterId(),
                oldNetworkCluster.getNetworkId()).isEmpty();
    }

    GlusterBrickDao getGlusterBrickDao() {
        return DbFacade.getInstance().getGlusterBrickDao();
    }
}
