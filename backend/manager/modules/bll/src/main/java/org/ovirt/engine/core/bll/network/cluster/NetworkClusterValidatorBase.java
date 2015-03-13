package org.ovirt.engine.core.bll.network.cluster;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.gluster.GlusterFeatureSupported;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsDAO;

/**
 * Validator class for {@link NetworkCluster} instances.
 */
public abstract class NetworkClusterValidatorBase {
    protected static final String NETWORK_NAME_REPLACEMENT = "$NetworkName %s";
    protected final NetworkCluster networkCluster;
    private final Version version;

    public NetworkClusterValidatorBase(NetworkCluster networkCluster, Version version) {
        this.networkCluster = networkCluster;
        this.version = version;
    }

    /**
     * Make sure the given {@link Network} belongs to the same DC as the cluster.
     *
     * @param cluster
     *            the cluster to be checked against
     * @param network
     *            network to be checked
     */
    public ValidationResult networkBelongsToClusterDataCenter(VDSGroup cluster, Network network) {
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_FROM_DIFFERENT_DC,
                String.format(NETWORK_NAME_REPLACEMENT, network.getName())).
                unless(cluster.getStoragePoolId().equals(network.getDataCenterId()));
    }

    public ValidationResult managementNetworkNotExternal(Network network) {
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_MANAGEMENT_NETWORK_CANNOT_BE_EXTERNAL,
                String.format(NETWORK_NAME_REPLACEMENT, network.getName())).when(
                        networkCluster.isManagement() &&
                        network.isExternal());
    }

    public ValidationResult managementNetworkRequired(Network network) {
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_MANAGEMENT_NETWORK_REQUIRED,
                String.format(NETWORK_NAME_REPLACEMENT, network.getName())).when(
                        networkCluster.isManagement() &&
                        !networkCluster.isRequired());
    }

    /**
     * Make sure the management network change is valid - that is allowed in an empty cluster only
     *
     * @return Error if the management network change is not allowed.
     */
    public ValidationResult managementNetworkChange() {
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_MANAGEMENT_NETWORK_CANNOT_BE_CHANGED).
                when(isManagementNetworkChangeInvalid());
    }

    protected boolean isManagementNetworkChangeInvalid() {
        return isManagementNetworkChanged() && isManagementNetworkChangeForbidden();
    }

    protected abstract boolean isManagementNetworkChanged();

    protected boolean isManagementNetworkChangeForbidden() {
        return !isClusterEmpty();
    }

    private boolean isClusterEmpty() {
        return getVdsDao().getAllForVdsGroup(networkCluster.getClusterId()).isEmpty();
    }

    VdsDAO getVdsDao() {
        return DbFacade.getInstance().getVdsDao();
    }

    /**
     * Make sure the migration network is valid.
     *
     * @return Error if the migration network feature is not supported and this network is marked as migration and is
     *         not the management network.
     */
    public ValidationResult migrationPropertySupported() {
        return networkCluster.isMigration() && !FeatureSupported.migrationNetwork(version)
                ? new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_MIGRATION_NETWORK_IS_NOT_SUPPORTED)
                : ValidationResult.VALID;
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

    /**
     * Make sure the gluster network is supported for the cluster version
     *
     * @param cluster
     * @return error if gluster network role is not supported for the compatibility version
     */
    public ValidationResult glusterNetworkSupported() {
        return networkCluster.isGluster()
                && !GlusterFeatureSupported.glusterNetworkRoleSupported(version)
                ? new ValidationResult(VdcBllMessages.GLUSTER_NETWORK_NOT_SUPPORTED_FOR_POOL_LEVEL)
                : ValidationResult.VALID;
    }
}
