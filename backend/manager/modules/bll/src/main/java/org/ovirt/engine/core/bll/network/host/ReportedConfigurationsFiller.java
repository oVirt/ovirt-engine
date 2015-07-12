package org.ovirt.engine.core.bll.network.host;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfigurations;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.NetworkInSyncWithVdsNetworkInterface;

@Singleton
public class ReportedConfigurationsFiller {

    private HostNetworkQosDao hostNetworkQosDao;
    private InterfaceDao interfaceDao;
    private NetworkDao networkDao;
    private VdsDao vdsDao;

    @Inject
    public ReportedConfigurationsFiller(HostNetworkQosDao hostNetworkQosDao,
        InterfaceDao interfaceDao,
        NetworkDao networkDao,
        VdsDao vdsDao) {

        this.hostNetworkQosDao = hostNetworkQosDao;
        this.interfaceDao = interfaceDao;
        this.networkDao = networkDao;
        this.vdsDao = vdsDao;
    }

    public void fillReportedConfigurations(List<NetworkAttachment> networkAttachments, Guid hostId) {

        List<VdsNetworkInterface> allInterfacesForHost = interfaceDao.getAllInterfacesForVds(hostId);
        Map<String, VdsNetworkInterface> networkNameToNicMap = nicsByNetworkId(allInterfacesForHost);

        BusinessEntityMap<Network> networkMap =
            new BusinessEntityMap<>(networkDao.getAllForCluster(vdsDao.get(hostId).getVdsGroupId()));

        QosDaoCache qosDaoCache = new QosDaoCache(hostNetworkQosDao);

        for (NetworkAttachment networkAttachment : networkAttachments) {
            fillReportedConfigurations(networkNameToNicMap, networkMap, qosDaoCache, networkAttachment);
        }
    }

    private void fillReportedConfigurations(Map<String, VdsNetworkInterface> networkNameToNicMap,
        BusinessEntityMap<Network> networkMap, QosDaoCache qosDaoCache, NetworkAttachment networkAttachment) {
        Network network = networkMap.get(networkAttachment.getNetworkId());
        HostNetworkQos networkQos = qosDaoCache.get(network.getQosId());

        VdsNetworkInterface nic =
            getNicToWhichIsNetworkAttached(networkNameToNicMap, networkMap, networkAttachment);

        //TODO MM: for case we have out of sync db with attachment which references inexisting network. Not sure if that's needed.
        if (nic != null) {
            ReportedConfigurations reportedConfigurations =
                createNetworkInSyncWithVdsNetworkInterface(network, networkQos, nic).reportConfigurationsOnHost();
            networkAttachment.setReportedConfigurations(reportedConfigurations);
        }
    }

    NetworkInSyncWithVdsNetworkInterface createNetworkInSyncWithVdsNetworkInterface(Network network,
        HostNetworkQos networkQos, VdsNetworkInterface nic) {
        return new NetworkInSyncWithVdsNetworkInterface(nic, network, networkQos);
    }

    //TODO MM: generify and use MapNetworkAttachments
    private Map<String, VdsNetworkInterface> nicsByNetworkId(List<VdsNetworkInterface> nics) {
        Map<String, VdsNetworkInterface> result = new HashMap<>();
        for (VdsNetworkInterface nic : nics) {
            if (nic.getNetworkName() != null) {
                result.put(nic.getNetworkName(), nic);
            }
        }
        return result;
    }

    private VdsNetworkInterface getNicToWhichIsNetworkAttached(Map<String, VdsNetworkInterface> networkNameToNicMap,
        BusinessEntityMap<Network> networkMap,
        NetworkAttachment networkAttachment) {
        Guid networkId = networkAttachment.getNetworkId();
        Network network = networkMap.get(networkId);
        return networkNameToNicMap.get(network.getName());
    }

    public void fillReportedConfiguration(NetworkAttachment networkAttachment, Guid hostId) {
        fillReportedConfigurations(Collections.singletonList(networkAttachment), hostId);
    }
}
