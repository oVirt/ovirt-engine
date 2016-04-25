package org.ovirt.engine.core.bll.network;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.common.action.PersistentHostSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.utils.NetworkUtils;

public class AddNetworksByLabelParametersBuilder extends HostSetupNetworksParametersBuilder {

    @Inject
    public AddNetworksByLabelParametersBuilder(InterfaceDao interfaceDao,
            VdsStaticDao vdsStaticDao,
            NetworkClusterDao networkClusterDao,
            NetworkAttachmentDao networkAttachmentDao) {
        super(interfaceDao, vdsStaticDao, networkClusterDao, networkAttachmentDao);
    }

    /**
     * Adds a list of labeled networks to a host
     */
    public PersistentHostSetupNetworksParameters buildParameters(Guid hostId,
            List<Network> labeledNetworks,
            Map<String, VdsNetworkInterface> nicsByLabel) {
        PersistentHostSetupNetworksParameters parameters = createHostSetupNetworksParameters(hostId);
        Set<Network> networksToAdd = getNetworksToConfigure(getNics(hostId), labeledNetworks);

        for (Network network : networksToAdd) {
            VdsNetworkInterface labeledNic = nicsByLabel.get(network.getLabel());
            VdsNetworkInterface nicToConfigure = getNicToConfigure(getNics(hostId), labeledNic.getId());
            if (nicToConfigure == null) {
                throw new EngineException(EngineError.LABELED_NETWORK_INTERFACE_NOT_FOUND);
            }

            // configure the network on the nic
            addAttachmentToParameters(nicToConfigure, network, parameters);
        }

        parameters.setNetworkNames(networksToAdd.stream().map(Network::getName).collect(Collectors.joining(", ")));

        return parameters;
    }

    private Set<Network> getNetworksToConfigure(List<VdsNetworkInterface> nics, List<Network> labeledNetworks) {
        Map<String, VdsNetworkInterface> nicsByNetworkName = NetworkUtils.hostInterfacesByNetworkName(nics);
        Set<Network> networkToAdd = new HashSet<>();

        for (Network network : labeledNetworks) {
            if (!nicsByNetworkName.containsKey(network.getName())) {
                networkToAdd.add(network);
            }
        }

        return networkToAdd;
    }
}
