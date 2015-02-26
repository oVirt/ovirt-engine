package org.ovirt.engine.core.bll.network.cluster;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;

public class UpdateNetworkClusterValidator extends NetworkClusterValidatorBase {

    private final NetworkCluster oldNetworkCluster;

    public UpdateNetworkClusterValidator(NetworkCluster networkCluster,
                                         NetworkCluster oldNetworkCluster,
                                         Version version) {
        super(networkCluster, version);

        this.oldNetworkCluster = oldNetworkCluster;
    }

    @Override
    protected boolean isManagementNetworkChanged() {
        return !oldNetworkCluster.isManagement() && networkCluster.isManagement();
    }

    public ValidationResult managementNetworkUnset() {
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_MANAGEMENT_NETWORK_UNSET).
                when(oldNetworkCluster.isManagement() && !networkCluster.isManagement());
    }

    public ValidationResult glusterNetworkInUseAndUnset(VDSGroup cluster) {
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_NETWORK_INUSE).
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
