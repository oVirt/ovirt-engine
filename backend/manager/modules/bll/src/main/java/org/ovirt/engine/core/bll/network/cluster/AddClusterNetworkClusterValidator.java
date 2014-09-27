package org.ovirt.engine.core.bll.network.cluster;

import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.compat.Version;

public class AddClusterNetworkClusterValidator extends NetworkClusterValidatorBase {

    public AddClusterNetworkClusterValidator(NetworkCluster networkCluster, Version version) {
        super(networkCluster, version);
    }

    @Override
    protected boolean isManagementNetworkChangeInvalid() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean isManagementNetworkChanged() {
        return false;
    }

}
