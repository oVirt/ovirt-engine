package org.ovirt.engine.core.bll.network.cluster;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Version;

/**
 * Validator class for {@link NetworkCluster} instances.
 */
public class NetworkClusterValidator {
    protected static final String NETWORK_NAME_REPLACEMENT = "$NetworkName %s";
    private final NetworkCluster networkCluster;
    private final Version version;

    public NetworkClusterValidator(NetworkCluster networkCluster, Version version) {
        this.networkCluster = networkCluster;
        this.version = version;
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

    /**
     * Make sure the migration network is valid.
     *
     * @param networkName
     *            The network's name.
     * @return Error if the migration network feature is not supported and this network is marked as migration and is
     *         not the management network.
     */
    public ValidationResult migrationPropertySupported(String networkName) {
        return !networkCluster.isMigration() || FeatureSupported.migrationNetwork(version) ? ValidationResult.VALID
                : new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_MIGRATION_NETWORK_IS_NOT_SUPPORTED);
    }

    /**
     * Make sure the external network attachment is supported for the version.
     *
     * @return Error iff the external network attachment is not supported.
     */
    public ValidationResult externalNetworkSupported() {
        return FeatureSupported.deviceCustomProperties(version)
                ? ValidationResult.VALID
                : new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_NOT_SUPPORTED);
    }

    /**
     * Make sure the external network attachment is valid: The network cannot be used as a display network.
     *
     * @param networkName
     *            The network's name.
     * @return Error iff the external network attachment is not valid.
     */
    public ValidationResult externalNetworkNotDisplay(String networkName) {
        return networkCluster.isDisplay() ?
                new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_CANNOT_BE_DISPLAY,
                        String.format(NETWORK_NAME_REPLACEMENT, networkName))
                : ValidationResult.VALID;
    }

    /**
     * Make sure the external network attachment is valid: The network cannot be required.
     *
     * @param networkName
     *            The network's name.
     * @return Error iff the external network attachment is not valid.
     */
    public ValidationResult externalNetworkNotRequired(String networkName) {
        return networkCluster.isRequired() ?
                new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_CANNOT_BE_REQUIRED,
                        String.format(NETWORK_NAME_REPLACEMENT, networkName))
                : ValidationResult.VALID;
    }

}
