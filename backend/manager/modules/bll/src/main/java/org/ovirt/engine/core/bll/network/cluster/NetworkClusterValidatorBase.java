package org.ovirt.engine.core.bll.network.cluster;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.NetworkUtils;

/**
 * Validator class for {@link NetworkCluster} instances.
 */
public abstract class NetworkClusterValidatorBase {

    protected static final String NETWORK_NAME_REPLACEMENT = "$NetworkName %s";
    private static final String NIC_NAME_REPLACEMENT = "$nicName %s";
    private static final String HOST_NAME_REPLACEMENT = "$hostName %s";

    protected final NetworkCluster networkCluster;

    private final InterfaceDao interfaceDao;
    private final NetworkDao networkDao;

    public NetworkClusterValidatorBase(InterfaceDao interfaceDao,
            NetworkDao networkDao,
            NetworkCluster networkCluster) {
        Objects.requireNonNull(interfaceDao, "interfaceDao cannot be null");
        Objects.requireNonNull(networkDao, "networkDao cannot be null");

        this.interfaceDao = interfaceDao;
        this.networkDao = networkDao;
        this.networkCluster = networkCluster;
    }

    public ValidationResult roleNetworkHasIp() {
        if (NetworkUtils.isRoleNetwork(networkCluster)) {
            final Network network = networkDao.get(networkCluster.getNetworkId());
            final String networkName = network.getName();
            final ValidationResult roleNetworkHasIpOnAttachedNics = roleNetworkHasIpOnAttachedNics(networkName);
            if (!roleNetworkHasIpOnAttachedNics.isValid()) {
                return roleNetworkHasIpOnAttachedNics;
            }
        }
        return ValidationResult.VALID;
    }

    ValidationResult roleNetworkHasIpOnAttachedNics(String networkName) {
        final VdsNetworkInterface missingIpNic = findMissingIpNic(networkName);
        if (missingIpNic != null) {
            return createMissingIpValidationResult(missingIpNic, networkName);
        }
        return ValidationResult.VALID;
    }

    private ValidationResult createMissingIpValidationResult(
            VdsNetworkInterface missingIpNic,
            String networkName) {

        return new ValidationResult(EngineMessage.NETWORK_ADDR_MANDATORY_FOR_ROLE_NETWORK,
                String.format(NETWORK_NAME_REPLACEMENT, networkName),
                String.format(NIC_NAME_REPLACEMENT, missingIpNic.getName()),
                String.format(HOST_NAME_REPLACEMENT, missingIpNic.getVdsName()));
    }

    private VdsNetworkInterface findMissingIpNic(final String networkName) {
        final List<VdsNetworkInterface> interfacesByClusterId =
                interfaceDao.getAllInterfacesByClusterId(networkCluster.getClusterId());
        final VdsNetworkInterface missingIpNic =
                interfacesByClusterId.stream().filter(nic -> networkName.equals(nic.getNetworkName()) &&
                        StringUtils.isEmpty(nic.getIpv4Address())).findFirst().orElse(null);

        return missingIpNic;
    }

    /**
     * Make sure the given {@link Network} belongs to the same DC as the cluster.
     *
     * @param cluster
     *            the cluster to be checked against
     * @param network
     *            network to be checked
     */
    public ValidationResult networkBelongsToClusterDataCenter(Cluster cluster, Network network) {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_FROM_DIFFERENT_DC,
                String.format(NETWORK_NAME_REPLACEMENT, network.getName())).
                unless(cluster.getStoragePoolId().equals(network.getDataCenterId()));
    }

    public ValidationResult managementNetworkNotExternal(Network network) {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_MANAGEMENT_NETWORK_CANNOT_BE_EXTERNAL,
                String.format(NETWORK_NAME_REPLACEMENT, network.getName())).when(
                networkCluster.isManagement() &&
                        network.isExternal());
    }

    public ValidationResult managementNetworkRequired(Network network) {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_MANAGEMENT_NETWORK_REQUIRED,
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
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_MANAGEMENT_NETWORK_CANNOT_BE_CHANGED).
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
        return getVdsDao().getAllForCluster(networkCluster.getClusterId()).isEmpty();
    }

    VdsDao getVdsDao() {
        return DbFacade.getInstance().getVdsDao();
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
                new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_CANNOT_BE_DISPLAY,
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
                new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_CANNOT_BE_REQUIRED,
                        String.format(NETWORK_NAME_REPLACEMENT, networkName))
                : ValidationResult.VALID;
    }
}
