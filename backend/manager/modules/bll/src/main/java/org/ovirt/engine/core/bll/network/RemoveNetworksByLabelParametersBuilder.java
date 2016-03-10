package org.ovirt.engine.core.bll.network;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.common.action.PersistentHostSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.utils.NetworkUtils;

public class RemoveNetworksByLabelParametersBuilder extends HostSetupNetworksParametersBuilder {

    @Inject
    public RemoveNetworksByLabelParametersBuilder(InterfaceDao interfaceDao,
            VdsStaticDao vdsStaticDao,
            NetworkClusterDao networkClusterDao,
            NetworkAttachmentDao networkAttachmentDao) {
        super(interfaceDao, vdsStaticDao, networkClusterDao, networkAttachmentDao);
    }

    /**
     * Removes a given list of labeled networks from a host
     */
    public PersistentHostSetupNetworksParameters buildParameters(Guid hostId, List<Network> networksToRemove) {
        PersistentHostSetupNetworksParameters parameters = createHostSetupNetworksParameters(hostId);
        Map<String, VdsNetworkInterface> nicByNetwork =
                NetworkUtils.hostInterfacesByNetworkName(getNics(hostId));
        Map<String, VdsNetworkInterface> nicByName =
                Entities.entitiesByName(getNics(hostId));

        for (Network networkToRemove : networksToRemove) {
            VdsNetworkInterface nicWithNetwork = nicByNetwork.get(networkToRemove.getName());

            if (nicWithNetwork != null) {
                VdsNetworkInterface baseNicWithNetwork = nicByName.get(NetworkCommonUtils.stripVlan(nicWithNetwork));
                if (NetworkUtils.isLabeled(networkToRemove) && NetworkUtils.isLabeled(baseNicWithNetwork)
                        && baseNicWithNetwork.getLabels().contains(networkToRemove.getLabel())) {
                    NetworkAttachment networkAttachment =
                            getNetworkIdToAttachmentMap(hostId).get(networkToRemove.getId());
                    if (networkAttachment == null) {
                        parameters.getRemovedUnmanagedNetworks().add(networkToRemove.getName());
                    } else {
                        parameters.getRemovedNetworkAttachments().add(networkAttachment.getId());
                    }
                }
            }
        }

        return parameters;
    }

}
