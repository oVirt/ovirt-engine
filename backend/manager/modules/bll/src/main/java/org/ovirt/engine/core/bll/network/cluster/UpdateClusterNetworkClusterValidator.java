package org.ovirt.engine.core.bll.network.cluster;

import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.compat.Version;

public class UpdateClusterNetworkClusterValidator extends NetworkClusterValidatorBase {

    public UpdateClusterNetworkClusterValidator(NetworkCluster networkCluster, Version version) {
        super(networkCluster, version);
    }

    @Override
    protected boolean isManagementNetworkChanged() {
        return true;
    }

}
