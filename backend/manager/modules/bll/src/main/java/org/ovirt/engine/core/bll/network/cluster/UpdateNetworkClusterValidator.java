package org.ovirt.engine.core.bll.network.cluster;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Version;

public class UpdateNetworkClusterValidator extends NetworkClusterValidatorBase {

    private final NetworkCluster oldNetworkCluster;

    public UpdateNetworkClusterValidator(NetworkCluster networkCluster,
                                         NetworkCluster oldNetworkCluster,
                                         Version version) {
        super(networkCluster, version);

        this.oldNetworkCluster = oldNetworkCluster;
    }

    protected boolean isManagementNetworkChanged() {
        return !oldNetworkCluster.isManagement() && networkCluster.isManagement();
    }

    public ValidationResult managementNetworkUnset() {
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_MANAGEMENT_NETWORK_UNSET).
                when(oldNetworkCluster.isManagement() && !networkCluster.isManagement());
    }
}
