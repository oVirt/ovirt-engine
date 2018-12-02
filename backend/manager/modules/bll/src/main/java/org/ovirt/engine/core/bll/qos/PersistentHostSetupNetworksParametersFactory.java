package org.ovirt.engine.core.bll.qos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.PersistentHostSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.utils.MapNetworkAttachments;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;

/**
 * Factory creates parameters to be used to refresh out of sync networks using
 * {@link ActionType#PersistentHostSetupNetworks}
 */
public class PersistentHostSetupNetworksParametersFactory {

    private final NetworkAttachmentDao networkAttachmentDao;

    @Inject
    private PersistentHostSetupNetworksParametersFactory(NetworkAttachmentDao networkAttachmentDao) {
        this.networkAttachmentDao = networkAttachmentDao;
    }

    /**
     * @param hostId host on which networks to be updated reside
     * @param networks networks to be updated; it is assumed, that all those networks belongs to given host.
     *
     * @return PersistentHostSetupNetworksParameters to refresh all given networks.
     */
    public PersistentHostSetupNetworksParameters create(Guid hostId,
            List<Network> networks) {

        Map<Network, NetworkAttachment> networksToSync = getNetworksToSync(hostId, networks);

        PersistentHostSetupNetworksParameters parameters = new PersistentHostSetupNetworksParameters(hostId);
        parameters.setRollbackOnFailure(true);
        parameters.setShouldBeLogged(false);
        parameters.setCommitOnSuccess(true);
        parameters.setNetworkNames(getNetworkNames(networksToSync.keySet()));
        parameters.setNetworkAttachments(new ArrayList<>(networksToSync.values()));

        return parameters;
    }

    /**
     * @param networks networks to be transformed to comma separated list of its names.
     * @return comma separated list of network names.
     */
    private String getNetworkNames(Collection<Network> networks) {
        return networks.stream().map(Network::getName).collect(Collectors.joining(", "));
    }

    /**
     * For given host and it's networks, return network attachments representing those networks on this host.
     * @param hostId host ID
     * @param networks networks to transform
     * @return network attachments representing given networks on this host.
     */
    private Map<Network, NetworkAttachment> getNetworksToSync(Guid hostId, List<Network> networks) {
        List<NetworkAttachment> allAttachmentsOfHost = networkAttachmentDao.getAllForHost(hostId);
        Map<Guid, NetworkAttachment> attachmentsByNetworkId =
                new MapNetworkAttachments(allAttachmentsOfHost).byNetworkId();

        Map<Network, NetworkAttachment> networksToSync = new HashMap<>();
        for (Network network : networks) {
            Guid networkId = network.getId();
            NetworkAttachment attachmentToSync = attachmentsByNetworkId.get(networkId);
            if (attachmentToSync != null && !attachmentToSync.isQosOverridden()) {
                attachmentToSync.setOverrideConfiguration(true);
                networksToSync.put(network, attachmentToSync);
            }
        }

        return networksToSync;
    }
}
