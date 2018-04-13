package org.ovirt.engine.core.bll.network.host;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface.NetworkImplementationDetails;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.vdsbroker.NetworkImplementationDetailsUtils;

public class HostNicsUtil {

    private final VdsStaticDao vdsStaticDao;
    private final NetworkDao networkDao;
    private final InterfaceDao interfaceDao;
    private final NetworkImplementationDetailsUtils networkImplementationDetailsUtils;

    @Inject
    HostNicsUtil(VdsStaticDao vdsStaticDao,
            InterfaceDao interfaceDao,
            NetworkDao networkDao,
            NetworkImplementationDetailsUtils networkImplementationDetailsUtils) {
        this.vdsStaticDao = Objects.requireNonNull(vdsStaticDao);
        this.interfaceDao = Objects.requireNonNull(interfaceDao);
        this.networkDao = Objects.requireNonNull(networkDao);
        this.networkImplementationDetailsUtils = Objects.requireNonNull(networkImplementationDetailsUtils);
    }

    public List<VdsNetworkInterface> findHostNics(Guid hostId, Guid userID, boolean isFiltered) {
        return findHostNics(hostId, this::findHostClusterId, userID, isFiltered);
    }

    private Guid findHostClusterId(Guid hostId) {
        final VdsStatic host = vdsStaticDao.get(hostId);
        return host.getClusterId();
    }

    private List<VdsNetworkInterface> findHostNics(Guid hostId,
            Function<Guid, Guid> hostClusterIdFinder,
            Guid userID,
            boolean isFiltered) {
        final List<VdsNetworkInterface> vdsInterfaces =
                interfaceDao.getAllInterfacesForVds(hostId, userID, isFiltered);

        // 1. here we return all interfaces (eth0, eth1, eth2) - the first
        // condition
        // 2. we also return bonds that connected to network and has interfaces
        // - the second condition
        // i.e.
        // we have:
        // Network | Interface
        // -------------------
        // red-> |->eth0
        // |->eth1
        // | |->eth2
        // blue-> |->bond0->|->eth3
        // |->bond1
        //
        // we return: eth0, eth1, eth2, eth3, bond0
        // we don't return bond1 because he is not connected to network and has
        // no child interfaces

        List<VdsNetworkInterface> interfaces = new ArrayList<>(vdsInterfaces.size());

        if (!vdsInterfaces.isEmpty()) {
            final Guid clusterId = hostClusterIdFinder.apply(hostId);
            Map<String, Network> networks =
                    Entities.entitiesByName(networkDao.getAllForCluster(clusterId));

            for (final VdsNetworkInterface nic : vdsInterfaces) {
                if (!nic.isBond() || nicDoesHaveSlaves(vdsInterfaces, nic)) {
                    interfaces.add(nic);
                    Network network = networks.get(nic.getNetworkName());

                    NetworkImplementationDetails networkImplementationDetails =
                            networkImplementationDetailsUtils.calculateNetworkImplementationDetails(nic, network);
                    nic.setNetworkImplementationDetails(networkImplementationDetails);
                }
            }
        }
        return interfaces;
    }

    private boolean nicDoesHaveSlaves(List<VdsNetworkInterface> vdsInterfaces, VdsNetworkInterface nic) {
        return getSlavesOfBond(vdsInterfaces, nic).size() > 0;
    }

    private List<VdsNetworkInterface> getSlavesOfBond(List<VdsNetworkInterface> vdsInterfaces,
            final VdsNetworkInterface nic) {
        return vdsInterfaces.stream().filter(bond -> StringUtils.equals(bond.getBondName(), nic.getName()))
                .collect(Collectors.toList());
    }
}
