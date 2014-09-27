package org.ovirt.engine.core.bll.network.cluster;

import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.compat.Version;

public class AttachNetworkClusterValidator extends NetworkClusterValidatorBase {

    public AttachNetworkClusterValidator(NetworkCluster networkCluster, Version version) {
        super(networkCluster, version);
    }

    protected boolean isManagementNetworkChanged() {
        return networkCluster.isManagement();
    }
}
