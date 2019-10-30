package org.ovirt.engine.core.bll.network.host;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.network.cluster.DefaultRouteUtil;
import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.network.DnsResolverConfiguration;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfigurations;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.network.DnsResolverConfigurationDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.NetworkInSyncWithVdsNetworkInterface;
import org.ovirt.engine.core.vdsbroker.EffectiveHostNetworkQos;

@Singleton
public class ReportedConfigurationsFiller {

    private final InterfaceDao interfaceDao;
    private final NetworkDao networkDao;
    private final VdsStaticDao vdsStaticDao;
    private final DnsResolverConfigurationDao dnsResolverConfigurationDao;
    private final ClusterDao clusterDao;
    private final EffectiveHostNetworkQos effectiveHostNetworkQos;
    private final DefaultRouteUtil defaultRouteUtil;

    @Inject
    public ReportedConfigurationsFiller(InterfaceDao interfaceDao,
            NetworkDao networkDao,
            VdsStaticDao vdsStaticDao,
            DnsResolverConfigurationDao dnsResolverConfigurationDao,
            ClusterDao clusterDao,
            EffectiveHostNetworkQos effectiveHostNetworkQos,
            DefaultRouteUtil defaultRouteUtil) {

        this.interfaceDao = Objects.requireNonNull(interfaceDao);
        this.networkDao = Objects.requireNonNull(networkDao);
        this.vdsStaticDao = Objects.requireNonNull(vdsStaticDao);
        this.dnsResolverConfigurationDao = Objects.requireNonNull(dnsResolverConfigurationDao);
        this.clusterDao = Objects.requireNonNull(clusterDao);
        this.effectiveHostNetworkQos = Objects.requireNonNull(effectiveHostNetworkQos);
        this.defaultRouteUtil = Objects.requireNonNull(defaultRouteUtil);
    }

    public void fillReportedConfigurations(List<VdsNetworkInterface> allInterfacesForHost,
            BusinessEntityMap<Network> networkMap,
            List<NetworkAttachment> networkAttachments,
            DnsResolverConfiguration reportedDnsResolverConfiguration,
            Guid clusterId) {
        Cluster cluster = clusterDao.get(clusterId);
        Map<String, VdsNetworkInterface> networkNameToNicMap = nicsByNetworkId(allInterfacesForHost);

        for (NetworkAttachment networkAttachment : networkAttachments) {
            fillReportedConfigurations(networkNameToNicMap,
                    networkMap,
                    networkAttachment,
                    reportedDnsResolverConfiguration,
                    cluster);
        }
    }

    public void fillReportedConfigurations(List<NetworkAttachment> networkAttachments, Guid hostId) {
        List<VdsNetworkInterface> allInterfacesForHost = interfaceDao.getAllInterfacesForVds(hostId);
        Guid clusterId = getClusterId(hostId);

        BusinessEntityMap<Network> networkMap = new BusinessEntityMap<>(networkDao.getAllForCluster(clusterId));
        fillReportedConfigurations(allInterfacesForHost,
                networkMap,
                networkAttachments,
                dnsResolverConfigurationDao.get(hostId),
                clusterId);
    }

    private Guid getClusterId(Guid hostId) {
        VdsStatic vdsStatic = vdsStaticDao.get(hostId);
        return vdsStatic.getClusterId();
    }

    private void fillReportedConfigurations(Map<String, VdsNetworkInterface> networkNameToNicMap,
            BusinessEntityMap<Network> networkMap,
            NetworkAttachment networkAttachment,
            DnsResolverConfiguration reportedDnsResolverConfiguration,
            Cluster cluster) {
        Network network = networkMap.get(networkAttachment.getNetworkId());

        VdsNetworkInterface nic =
                getNicToWhichIsNetworkAttached(networkNameToNicMap, networkMap, networkAttachment);

        if (nic != null) {
            NetworkInSyncWithVdsNetworkInterface isInSync =
                    createNetworkInSyncWithVdsNetworkInterface(networkAttachment,
                            nic,
                            network,
                            reportedDnsResolverConfiguration,
                            cluster);
            ReportedConfigurations reportedConfigurations = isInSync.reportConfigurationsOnHost();
            networkAttachment.setReportedConfigurations(reportedConfigurations);
        }
    }

    NetworkInSyncWithVdsNetworkInterface createNetworkInSyncWithVdsNetworkInterface(NetworkAttachment networkAttachment,
            VdsNetworkInterface nic,
            Network network,
            DnsResolverConfiguration reportedDnsResolverConfiguration,
            Cluster cluster) {

        HostNetworkQos hostNetworkQos = effectiveHostNetworkQos.getQos(networkAttachment, network);
        return new NetworkInSyncWithVdsNetworkInterface(nic,
                network,
                hostNetworkQos,
                networkAttachment,
                reportedDnsResolverConfiguration,
                cluster,
                this.defaultRouteUtil.isDefaultRouteNetwork(network.getId(), cluster.getId())
        );
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
