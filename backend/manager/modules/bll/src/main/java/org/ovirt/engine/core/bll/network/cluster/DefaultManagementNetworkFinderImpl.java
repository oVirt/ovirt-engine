package org.ovirt.engine.core.bll.network.cluster;

import java.util.List;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.Validate;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkDao;

@Singleton
final class DefaultManagementNetworkFinderImpl implements DefaultManagementNetworkFinder {

    private final NetworkDao networkDao;
    private final ManagementNetworkUtil managementNetworkUtil;
    private final Predicate<Network> managementNetworkCandidatePredicate;

    @Inject
    DefaultManagementNetworkFinderImpl(NetworkDao networkDao,
            ManagementNetworkUtil managementNetworkUtil,
            @Named("managementNetworkCandidatePredicate") Predicate<Network> managementNetworkCandidatePredicate) {
        Validate.notNull(networkDao, "networkDao cannot be null");
        Validate.notNull(managementNetworkUtil, "managementNetworkUtil cannot be null");
        Validate.notNull(managementNetworkCandidatePredicate, "managementNetworkCandidatePredicate cannot be null");

        this.networkDao = networkDao;
        this.managementNetworkUtil = managementNetworkUtil;
        this.managementNetworkCandidatePredicate = managementNetworkCandidatePredicate;
    }

    /**
     * The algorithm for finding the default management network is:
     * <ol>
     * <li>Use the default management network defined by the default name (according to
     * <code>ConfigValues.DefaultManagementNetwork</code>), if exists in the given data center (for backward
     * compatibility).</li>
     * <li>If the data center has a single network and that suitable to serve as themangement one use that.</li>
     * <li>If the data center has a single management network use that.</li>
     * <li>Otherwise return <code>null</code></li>
     * </ol>
     *
     * @see DefaultManagementNetworkFinder#findDefaultManagementNetwork(Guid)
     */
    @Override
    public Network findDefaultManagementNetwork(Guid dataCenterId) {
        final Network defaultEngineManagementNetwork = findConfigDefaultEngineManagementNetwork(dataCenterId);
        if (defaultEngineManagementNetwork != null) {
            return defaultEngineManagementNetwork;
        }
        final List<Network> dcNetworks = networkDao.getAllForDataCenter(dataCenterId);
        if (dcNetworks.size() == 1) {
            final Network singleDcNetwork = dcNetworks.get(0);
            if (managementNetworkCandidatePredicate.test(singleDcNetwork)) {
                return singleDcNetwork;
            }
        } else {
            final List<Network> managementNetworks = networkDao.getManagementNetworks(dataCenterId);
            if (managementNetworks.size() == 1) {
                return managementNetworks.get(0);
            }
        }
        return null;
    }

    private Network findConfigDefaultEngineManagementNetwork(Guid dataCenterId) {
        return networkDao.getByNameAndDataCenter(
                managementNetworkUtil.getDefaultManagementNetworkName(),
                dataCenterId);
    }
}
