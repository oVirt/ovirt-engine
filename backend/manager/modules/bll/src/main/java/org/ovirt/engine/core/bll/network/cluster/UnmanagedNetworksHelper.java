package org.ovirt.engine.core.bll.network.cluster;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.UnmanagedNetwork;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

@Singleton
public class UnmanagedNetworksHelper {
    private VdsDao vdsDao;
    private InterfaceDao interfaceDao;
    private NetworkDao networkDao;


    @Inject
    public UnmanagedNetworksHelper(VdsDao vdsDao, InterfaceDao interfaceDao, NetworkDao networkDao) {
        this.vdsDao = vdsDao;
        this.interfaceDao = interfaceDao;
        this.networkDao = networkDao;
    }

    public List<UnmanagedNetwork> getUnmanagedNetworks(Guid hostId) {
        List<UnmanagedNetwork> unmanagedNetworks = new ArrayList<>();
        VDS host = vdsDao.get(hostId);

        if (host != null) {
            List<VdsNetworkInterface> hostNetworkInterfaces = interfaceDao.getAllInterfacesForVds(hostId);

            BusinessEntityMap<Network> clusterNetworkMap = new BusinessEntityMap<>(getClusterNetworks(host));

            for (VdsNetworkInterface vdsNetworkInterface : hostNetworkInterfaces) {
                String networkName = vdsNetworkInterface.getNetworkName();
                if (networkName != null && !clusterNetworkMap.containsKey(networkName)) {
                    unmanagedNetworks.add(createUnmanagedNetworkEntity(vdsNetworkInterface, networkName));
                }
            }
        }
        return unmanagedNetworks;
    }

    private List<Network> getClusterNetworks(VDS host) {
        return networkDao.getAllForCluster(host.getClusterId());
    }

    private UnmanagedNetwork createUnmanagedNetworkEntity(VdsNetworkInterface vdsNetworkInterface, String networkName) {
        return new UnmanagedNetwork()
                .setNetworkName(networkName)
                .setNicId(vdsNetworkInterface.getId())
                .setNicName(vdsNetworkInterface.getName());
    }

    public UnmanagedNetwork getUnmanagedNetwork(Guid hostId, String networkName) {
        for (UnmanagedNetwork unmanagedNetwork : getUnmanagedNetworks(hostId)) {
            if (unmanagedNetwork.getNetworkName().equals(networkName)) {
                return unmanagedNetwork;
            }
        }

        return null;
    }
}
