package org.ovirt.engine.core.vdsbroker;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.network.cluster.DefaultRouteUtil;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface.NetworkImplementationDetails;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.network.DnsResolverConfigurationDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.NetworkInSyncWithVdsNetworkInterface;

@Singleton
public class NetworkImplementationDetailsUtils {
    private final NetworkAttachmentDao networkAttachmentDao;
    private final DefaultRouteUtil defaultRouteUtil;
    private final EffectiveHostNetworkQos effectiveHostNetworkQos;
    private final VdsStaticDao vdsStaticDao;
    private final ClusterDao clusterDao;
    private final InterfaceDao interfaceDao;
    private final NetworkDao networkDao;
    private final DnsResolverConfigurationDao dnsResolverConfigurationDao;
    private final CalculateBaseNic calculateBaseNic;

    @Inject
    public NetworkImplementationDetailsUtils(EffectiveHostNetworkQos effectiveHostNetworkQos,
            NetworkAttachmentDao networkAttachmentDao,
            VdsStaticDao vdsStaticDao,
            ClusterDao clusterDao,
            InterfaceDao interfaceDao,
            NetworkDao networkDao,
            DnsResolverConfigurationDao dnsResolverConfigurationDao,
            CalculateBaseNic calculateBaseNic,
            DefaultRouteUtil defaultRouteUtil) {

        this.effectiveHostNetworkQos = Objects.requireNonNull(effectiveHostNetworkQos);
        this.vdsStaticDao = Objects.requireNonNull(vdsStaticDao);
        this.clusterDao = Objects.requireNonNull(clusterDao);
        this.interfaceDao = Objects.requireNonNull(interfaceDao);
        this.networkDao = Objects.requireNonNull(networkDao);
        this.dnsResolverConfigurationDao = Objects.requireNonNull(dnsResolverConfigurationDao);
        this.calculateBaseNic = Objects.requireNonNull(calculateBaseNic);
        this.networkAttachmentDao = Objects.requireNonNull(networkAttachmentDao);
        this.defaultRouteUtil = Objects.requireNonNull(defaultRouteUtil);
    }

    public Set<VdsNetworkInterface> getAllInterfacesOutOfSync(Guid clusterId) {
        Map<String, Network> clusterNetworksByName = networkDao.getNetworksForCluster(clusterId);
        Map<Guid, HostNetworkQos> qosByNetworkId = calcQosByNetworkIdMap(clusterNetworksByName.values());
        Cluster cluster = clusterDao.get(clusterId);
        return interfaceDao.getAllInterfacesByClusterId(clusterId)
                .stream()
                .filter(iface -> clusterNetworksByName.get(iface.getNetworkName()) != null)
                .filter(iface -> {
                    Network network = clusterNetworksByName.get(iface.getNetworkName());
                    HostNetworkQos qos = qosByNetworkId.get(network.getId());
                    return isNetworkOutOfSync(iface, network, cluster, qos);
                })
                .collect(Collectors.toSet());

    }

    private Map<Guid, HostNetworkQos> calcQosByNetworkIdMap(Collection<Network> networks) {
        Map<Guid, HostNetworkQos> qosByNetworkId = new HashMap<>();
        List<HostNetworkQos> allQos = effectiveHostNetworkQos.getAll();
        networks.forEach(net -> {
            var netQos = allQos.stream().filter(qos -> qos.getId().equals(net.getQosId())).findFirst();
            netQos.ifPresent(qos -> qosByNetworkId.put(net.getId(), qos));
        });
        return qosByNetworkId;
    }

    private boolean isNetworkOutOfSync(VdsNetworkInterface iface, Network network, Cluster cluster, HostNetworkQos hostNetworkQos) {
        return isNetworkOutOfSync(calculateNetworkImplementationDetails(iface, network, cluster, hostNetworkQos));
    }

    private boolean isNetworkOutOfSync(NetworkImplementationDetails details) {
        return details != null && details.isManaged() && !details.isInSync();
    }

    /**
     * Fill network details for the given network devices from the given networks.<br>
     * {@link NetworkImplementationDetails#isInSync()} will be <code>true</code> IFF the logical network
     * properties are exactly the same as those defined on the network device.
     * @param network
     *            The network definition to fill the details from.
     * @param iface
     *            The network device to update.
     */
    public NetworkImplementationDetails calculateNetworkImplementationDetails(VdsNetworkInterface iface, Network network,
            Cluster cluster) {
        HostNetworkQos hostNetworkQos = effectiveHostNetworkQos.getHostNetworkQosFromNetwork(network);
        return calculateNetworkImplementationDetails(iface, network, cluster, hostNetworkQos);
    }

    private NetworkImplementationDetails calculateNetworkImplementationDetails(
            VdsNetworkInterface iface, Network network, Cluster cluster, HostNetworkQos hostNetworkQos) {

        if (iface == null || StringUtils.isEmpty(iface.getNetworkName())) {
            return null;
        }

        if (network != null) {
            boolean networkInSync = build(iface, network, cluster, hostNetworkQos).isNetworkInSync();
            return new NetworkImplementationDetails(networkInSync, true);
        } else {
            return new NetworkImplementationDetails();
        }
    }

    public NetworkImplementationDetails calculateNetworkImplementationDetails(VdsNetworkInterface iface, Network network) {
        Cluster cluster = getCluster(iface.getVdsId());
        return calculateNetworkImplementationDetails(iface, network, cluster);
    }

    private NetworkInSyncWithVdsNetworkInterface build(NetworkAttachment networkAttachment,
            VdsNetworkInterface vdsNetworkInterface,
            Network network, Cluster cluster, HostNetworkQos hostNetworkQos) {

        Guid vdsId = vdsNetworkInterface.getVdsId();

        HostNetworkQos qos = effectiveHostNetworkQos.selectQos(networkAttachment, hostNetworkQos);
        return new NetworkInSyncWithVdsNetworkInterface(vdsNetworkInterface,
                network,
                qos,
                networkAttachment,
                dnsResolverConfigurationDao.get(vdsId),
                cluster,
                defaultRouteUtil.isDefaultRouteNetwork(network.getId(), cluster.getId())
        );
    }

    private Cluster getCluster(Guid vdsId) {
        VdsStatic vdsStatic = vdsStaticDao.get(vdsId);
        Guid clusterId = vdsStatic.getClusterId();
        return clusterDao.get(clusterId);
    }


    private NetworkInSyncWithVdsNetworkInterface build(VdsNetworkInterface nic, Network network, Cluster cluster, HostNetworkQos qos) {
        NetworkAttachment networkAttachment = getNetworkAttachmentForNicAndNetwork(nic, network);
        return build(networkAttachment, nic, network, cluster, qos);
    }

    private NetworkAttachment getNetworkAttachmentForNicAndNetwork(VdsNetworkInterface nic, Network network) {
        VdsNetworkInterface baseNic = calculateBaseNic.getBaseNic(nic);
        return networkAttachmentDao.getNetworkAttachmentByNicIdAndNetworkId(baseNic.getId(), network.getId());
    }
}
