package org.ovirt.engine.core.bll.network.cluster;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.dal.VdcBllMessages;

/**
 * Validator class for {@link NetworkCluster} instances.
 */
public class NetworkClusterValidator {
    protected static final String NETWORK_NAME_REPLACEMENT = "$NetworkName %s";
    private NetworkCluster networkCluster;

    public NetworkClusterValidator(NetworkCluster networkCluster) {
        this.networkCluster = networkCluster;
    }

    /**
     * Make sure the management network attachment is valid: The network must be required.
     *
     * @param networkName
     *            The network's name.
     * @return Error iff the management network attachment is not valid.
     */
    public ValidationResult managementNetworkAttachment(String networkName) {
        return networkCluster.isRequired() ? ValidationResult.VALID
                : new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_MANAGEMENT_NETWORK_REQUIRED,
                        String.format(NETWORK_NAME_REPLACEMENT, networkName));
    }
}
