package org.ovirt.engine.core.bll.network.cluster.transformer;

import static org.ovirt.engine.core.utils.CollectionUtils.nullToEmptyList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.ManageNetworksParametersBuilder;
import org.ovirt.engine.core.bll.network.ManageNetworksParametersBuilderFactory;
import org.ovirt.engine.core.common.action.PersistentHostSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.vdsbroker.NetworkImplementationDetailsUtils;

final class NetworkClustersToSetupNetworksParametersTransformerImpl
        implements NetworkClustersToSetupNetworksParametersTransformer {

    private final NetworkDao networkDao;
    private final InterfaceDao interfaceDao;
    private final VdsStaticDao vdsStaticDao;
    private final NetworkClusterDao networkClusterDao;
    private final NetworkAttachmentDao networkAttachmentDao;
    private final ManageNetworksParametersBuilderFactory manageNetworksParametersBuilderFactory;
    private final CommandContext commandContext;
    private final NetworkImplementationDetailsUtils networkImplementationDetailsUtils;

    NetworkClustersToSetupNetworksParametersTransformerImpl(
            NetworkDao networkDao,
            InterfaceDao interfaceDao,
            VdsStaticDao vdsStaticDao,
            NetworkClusterDao networkClusterDao,
            NetworkAttachmentDao networkAttachmentDao,
            ManageNetworksParametersBuilderFactory manageNetworksParametersBuilderFactory,
            NetworkImplementationDetailsUtils networkImplementationDetailsUtils,
            CommandContext commandContext) {
        Objects.requireNonNull(networkDao, "networkDao cannot be null");
        Objects.requireNonNull(interfaceDao, "interfaceDao cannot be null");
        Objects.requireNonNull(vdsStaticDao, "vdsStaticDao cannot be null");
        Objects.requireNonNull(networkClusterDao, "networkClusterDao cannot be null");
        Objects.requireNonNull(networkAttachmentDao, "networkAttachmentDao cannot be null");
        Objects.requireNonNull(manageNetworksParametersBuilderFactory,
                "manageLabeledNetworksParametersBuilderFactory cannot be null");
        Objects.requireNonNull(networkImplementationDetailsUtils, "networkImplementationDetailsUtils cannot be null");

        this.networkDao = networkDao;
        this.interfaceDao = interfaceDao;
        this.vdsStaticDao = vdsStaticDao;
        this.networkClusterDao = networkClusterDao;
        this.networkAttachmentDao = networkAttachmentDao;
        this.manageNetworksParametersBuilderFactory = manageNetworksParametersBuilderFactory;
        this.networkImplementationDetailsUtils = networkImplementationDetailsUtils;
        this.commandContext = commandContext;
    }

    @Override
    public List<PersistentHostSetupNetworksParameters> transform(
            Collection<NetworkCluster> attachments,
            Collection<NetworkCluster> detachments,
            Collection<NetworkCluster> updates) {

        final Map<Guid, List<Network>> attachNetworksByHost = new HashMap<>();
        final Map<Guid, Map<String, VdsNetworkInterface>> labelsToNicsByHost = new HashMap<>();

        for (NetworkCluster networkCluster : attachments) {
            final Network network = networkDao.get(networkCluster.getNetworkId());
            final List<VdsNetworkInterface> nics = interfaceDao.getAllInterfacesByLabelForCluster(
                    networkCluster.getClusterId(),
                    network.getLabel());

            for (VdsNetworkInterface nic : nics) {
                if (!attachNetworksByHost.containsKey(nic.getVdsId())) {
                    attachNetworksByHost.put(nic.getVdsId(), new ArrayList<>());
                    labelsToNicsByHost.put(nic.getVdsId(), new HashMap<>());
                }

                labelsToNicsByHost.get(nic.getVdsId()).put(network.getLabel(), nic);
                attachNetworksByHost.get(nic.getVdsId()).add(network);
            }
        }

        Map<Guid, List<Network>> detachNetworksByHost = new HashMap<>();

        for (NetworkCluster networkCluster : detachments) {
            final Network network = networkDao.get(networkCluster.getNetworkId());
            final List<VdsNetworkInterface> nics = interfaceDao.getAllInterfacesByLabelForCluster(
                    networkCluster.getClusterId(),
                    network.getLabel());

            for (VdsNetworkInterface nic : nics) {
                if (!detachNetworksByHost.containsKey(nic.getVdsId())) {
                    detachNetworksByHost.put(nic.getVdsId(), new ArrayList<>());
                }

                detachNetworksByHost.get(nic.getVdsId()).add(network);
            }
        }

        return createSetupNetworksParameters(attachNetworksByHost,
                labelsToNicsByHost,
                detachNetworksByHost,
                getUpdatedNetworksByHost(updates));
    }

    /**
     * @param updates NetworkCluster instances related to updated networks
     * @return Mapping HostId to list of updated networks, which relates to NetworkCluster from parameter.
     */
    private Map<Guid, List<Network>> getUpdatedNetworksByHost(Collection<NetworkCluster> updates) {
        Map<Guid, List<Network>> result = new HashMap<>();

        Map<Guid, List<NetworkCluster>> updatesByClusterId = updates.stream()
                .collect(Collectors.groupingBy(NetworkCluster::getClusterId));

        Set<Guid> idsOfUpdatedNetworks = updates.stream().map(NetworkCluster::getNetworkId).collect(Collectors.toSet());

        for (Guid clusterId : updatesByClusterId.keySet()) {
            Map<String, Network> clusterNetworksByName = networkDao.getAllForCluster(clusterId)
                    .stream()
                    .filter(network->idsOfUpdatedNetworks.contains(network.getId()))
                    .collect(Collectors.toMap(Network::getName, Function.identity()));

            List<VdsNetworkInterface> interfacesOfCluster = interfaceDao.getAllInterfacesByClusterId(clusterId);

            for (VdsNetworkInterface iface : interfacesOfCluster) {
                if (!clusterNetworksByName.containsKey(iface.getNetworkName())) {
                    continue;
                }

                Network network = clusterNetworksByName.get(iface.getNetworkName());
                Guid vdsId = iface.getVdsId();

                VdsNetworkInterface.NetworkImplementationDetails networkImplementationDetails =
                        networkImplementationDetailsUtils.calculateNetworkImplementationDetails(iface, network);
                boolean networkShouldBeSynced =
                        networkImplementationDetails != null && !networkImplementationDetails.isInSync();

                if (networkShouldBeSynced) {
                    if (!result.containsKey(vdsId)) {
                        result.put(vdsId, new ArrayList<>());
                    }

                    result.get(vdsId).add(network);
                }
            }
        }

        return result;
    }

    private List<PersistentHostSetupNetworksParameters> createSetupNetworksParameters(
            Map<Guid, List<Network>> attachNetworksByHost,
            Map<Guid, Map<String, VdsNetworkInterface>> labelsToNicsByHost,
            Map<Guid, List<Network>> detachNetworksByHost,
            Map<Guid, List<Network>> updates) {

        final List<PersistentHostSetupNetworksParameters> parameters = new ArrayList<>(attachNetworksByHost.size());
        final ManageNetworksParametersBuilder builder =
                manageNetworksParametersBuilderFactory.create(commandContext,
                        interfaceDao,
                        vdsStaticDao,
                        networkClusterDao,
                        networkAttachmentDao);

        Set<Guid> hostIds = Stream.of(attachNetworksByHost, detachNetworksByHost, updates)
                .flatMap(e -> e.keySet().stream())
                .collect(Collectors.toSet());

        for (Guid hostId : hostIds) {
            final Map<String, VdsNetworkInterface> nicsByLabel = labelsToNicsByHost.get(hostId);

            parameters.add(builder.buildParameters(
                    hostId,
                    nullToEmptyList(attachNetworksByHost.get(hostId)),
                    nullToEmptyList(detachNetworksByHost.get(hostId)),
                    nicsByLabel == null ? Collections.emptyMap() : nicsByLabel,
                    nullToEmptyList(updates.get(hostId))));
        }

        return parameters;
    }
}
