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
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.NetworkInSyncWithVdsNetworkInterface;
import org.ovirt.engine.core.vdsbroker.EffectiveHostNetworkQos;

@Singleton
public class ReportedConfigurationsFiller {

    private final InterfaceDao interfaceDao;
    private final NetworkDao networkDao;
    private final VdsDao vdsDao;
    private EffectiveHostNetworkQos effectiveHostNetworkQos;

    @Inject
    public ReportedConfigurationsFiller(InterfaceDao interfaceDao,
        NetworkDao networkDao,
        VdsDao vdsDao,
        EffectiveHostNetworkQos effectiveHostNetworkQos) {

        this.interfaceDao = interfaceDao;
        this.networkDao = networkDao;
        this.vdsDao = vdsDao;
        this.effectiveHostNetworkQos = effectiveHostNetworkQos;
    }

    public void fillReportedConfigurations(List<NetworkAttachment> networkAttachments, Guid hostId) {

        List<VdsNetworkInterface> allInterfacesForHost = interfaceDao.getAllInterfacesForVds(hostId);
        Map<String, VdsNetworkInterface> networkNameToNicMap = nicsByNetworkId(allInterfacesForHost);

        BusinessEntityMap<Network> networkMap =
            new BusinessEntityMap<>(networkDao.getAllForCluster(vdsDao.get(hostId).getClusterId()));

        for (NetworkAttachment networkAttachment : networkAttachments) {
            fillReportedConfigurations(networkNameToNicMap, networkMap, networkAttachment);
        }
    }

    private void fillReportedConfigurations(Map<String, VdsNetworkInterface> networkNameToNicMap,
        BusinessEntityMap<Network> networkMap,
        NetworkAttachment networkAttachment) {
        Network network = networkMap.get(networkAttachment.getNetworkId());

        VdsNetworkInterface nic =
            getNicToWhichIsNetworkAttached(networkNameToNicMap, networkMap, networkAttachment);

        if (nic != null) {
            ReportedConfigurations reportedConfigurations =
                createNetworkInSyncWithVdsNetworkInterface(networkAttachment, nic, network).reportConfigurationsOnHost();
            networkAttachment.setReportedConfigurations(reportedConfigurations);
        }
    }

    NetworkInSyncWithVdsNetworkInterface createNetworkInSyncWithVdsNetworkInterface(NetworkAttachment networkAttachment,
        VdsNetworkInterface nic,
        Network network) {

        HostNetworkQos hostNetworkQos = effectiveHostNetworkQos.getQos(networkAttachment, network);
        return new NetworkInSyncWithVdsNetworkInterface(nic, network, hostNetworkQos, networkAttachment == null ? null
                : networkAttachment.getIpConfiguration());
    }

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
