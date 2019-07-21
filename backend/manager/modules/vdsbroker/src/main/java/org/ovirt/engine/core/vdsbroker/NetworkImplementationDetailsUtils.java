package org.ovirt.engine.core.vdsbroker;

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
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
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
    private final VdsDynamicDao vdsDynamicDao;
    private final ClusterDao clusterDao;
    private final InterfaceDao interfaceDao;
    private final NetworkDao networkDao;
    private final CalculateBaseNic calculateBaseNic;

    @Inject
    public NetworkImplementationDetailsUtils(EffectiveHostNetworkQos effectiveHostNetworkQos,
            NetworkAttachmentDao networkAttachmentDao,
            VdsStaticDao vdsStaticDao,
            VdsDynamicDao vdsDynamicDao,
            ClusterDao clusterDao,
            InterfaceDao interfaceDao,
            NetworkDao networkDao,
            CalculateBaseNic calculateBaseNic,
            DefaultRouteUtil defaultRouteUtil) {

        this.effectiveHostNetworkQos = Objects.requireNonNull(effectiveHostNetworkQos);
        this.vdsStaticDao = Objects.requireNonNull(vdsStaticDao);
        this.vdsDynamicDao = Objects.requireNonNull(vdsDynamicDao);
        this.clusterDao = Objects.requireNonNull(clusterDao);
        this.interfaceDao = Objects.requireNonNull(interfaceDao);
        this.networkDao = Objects.requireNonNull(networkDao);
        this.calculateBaseNic = Objects.requireNonNull(calculateBaseNic);
        this.networkAttachmentDao = Objects.requireNonNull(networkAttachmentDao);
        this.defaultRouteUtil = Objects.requireNonNull(defaultRouteUtil);
    }

    public Set<VdsNetworkInterface> getAllInterfacesOutOfSync(Guid clusterId) {
        Map<String, Network> clusterNetworksByName = networkDao
                .getNetworksForCluster(clusterId);

        return interfaceDao.getAllInterfacesByClusterId(clusterId)
            .stream()
            .filter(iface -> isNetworkOutOfSync(iface, clusterNetworksByName.get(iface.getNetworkName())))
            .collect(Collectors.toSet());

    }

    public boolean isNetworkOutOfSync(VdsNetworkInterface iface, Network network) {
        return isNetworkOutOfSync(calculateNetworkImplementationDetails(iface, network));
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
    public NetworkImplementationDetails calculateNetworkImplementationDetails(VdsNetworkInterface iface,
        Network network) {

        if (iface == null || StringUtils.isEmpty(iface.getNetworkName())) {
            return null;
        }

        if (network != null) {
            boolean networkInSync = build(iface, network).isNetworkInSync();
            return new NetworkImplementationDetails(networkInSync, true);
        } else {
            return new NetworkImplementationDetails();
        }
    }

    private NetworkInSyncWithVdsNetworkInterface build(NetworkAttachment networkAttachment,
            VdsNetworkInterface vdsNetworkInterface,
            Network network) {

        Guid vdsId = vdsNetworkInterface.getVdsId();
        Cluster cluster = getCluster(vdsId);

        HostNetworkQos hostNetworkQos = effectiveHostNetworkQos.getQos(networkAttachment, network);
        return new NetworkInSyncWithVdsNetworkInterface(vdsNetworkInterface,
                network,
                hostNetworkQos,
                networkAttachment,
                vdsDynamicDao.get(vdsId).getReportedDnsResolverConfiguration(),
                cluster,
                defaultRouteUtil.isDefaultRouteNetwork(network.getId(), cluster.getId())
        );
    }

    private Cluster getCluster(Guid vdsId) {
        VdsStatic vdsStatic = vdsStaticDao.get(vdsId);
        Guid clusterId = vdsStatic.getClusterId();
        return clusterDao.get(clusterId);
    }


    private NetworkInSyncWithVdsNetworkInterface build(VdsNetworkInterface nic, Network network) {
        NetworkAttachment networkAttachment = getNetworkAttachmentForNicAndNetwork(nic, network);
        return build(networkAttachment, nic, network);
    }

    private NetworkAttachment getNetworkAttachmentForNicAndNetwork(VdsNetworkInterface nic, Network network) {
        VdsNetworkInterface baseNic = calculateBaseNic.getBaseNic(nic);
        return networkAttachmentDao.getNetworkAttachmentByNicIdAndNetworkId(baseNic.getId(), network.getId());
    }
}
