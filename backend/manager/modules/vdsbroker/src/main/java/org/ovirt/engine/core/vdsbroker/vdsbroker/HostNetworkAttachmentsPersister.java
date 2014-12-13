package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class HostNetworkAttachmentsPersister {

    static final String INCONSISTENCY_NETWORK_IS_REPORTED_ON_DIFFERENT_NIC_THAN_WAS_SPECIFIED = "Inconsistency in current state and reported data: given network is reported on different nic than was specified.";
    private final NetworkAttachmentDao networkAttachmentDao;
    private final Guid hostId;
    private final List<NetworkAttachment> userNetworkAttachments;
    private final Map<String, VdsNetworkInterface> nicsByName;

    /**
     * map networkID-->NIC for each network assigned to one of given nics. Value of mapping is such mapped NIC.
     */
    private final Map<Guid, VdsNetworkInterface> reportedNicsByNetworkId;

    /**
     * map networkID-->Network for each network assigned to one of given nics.
     */
    private final Map<Guid, Network> reportedNetworksById;
    private final BusinessEntityMap<Network> clusterNetworks;

    public HostNetworkAttachmentsPersister(final NetworkAttachmentDao networkAttachmentDao,
            final Guid hostId,
            final List<VdsNetworkInterface> nics,
            final List<NetworkAttachment> userNetworkAttachments,
            final List<Network> clusterNetworks) {
        this.networkAttachmentDao = networkAttachmentDao;
        this.hostId = hostId;
        this.userNetworkAttachments = userNetworkAttachments;
        this.clusterNetworks = new BusinessEntityMap<>(clusterNetworks);
        nicsByName = Entities.entitiesByName(nics);

        reportedNetworksById = new HashMap<>();
        reportedNicsByNetworkId = new HashMap<>();
        initReportedNetworksAndNics(nics);
    }

    private void initReportedNetworksAndNics(List<VdsNetworkInterface> nics) {
        for (VdsNetworkInterface nic : nics) {
            if (nic.getNetworkName() != null) {
                Network network = clusterNetworks.get(nic.getNetworkName());
                if (network != null) {
                    reportedNetworksById.put(network.getId(), network);
                    reportedNicsByNetworkId.put(network.getId(), nic);
                }
            }
        }
    }

    public void persistNetworkAttachments() {
        List<NetworkAttachment> networkAttachments = networkAttachmentDao.getAllForHost(hostId);
        removeInvalidNetworkAttachmentsFromDb(networkAttachments);

        persistOrUpdateUserNetworkAttachments(networkAttachments);
        updateReportedNetworkAttachmentsNotMentionedInRequest(networkAttachments);
    }

    /**
     * Adds new network attachments for reported networks from the host which weren't associated to a user attachment.
     * Updates existing network attachments which network get reported on different nic.
     * <br/>
     * The network attachment's attributes will be inherited from the network interface on which it is configured.
     * @param networkAttachments already existing or newly created network attachments
     */
    private void updateReportedNetworkAttachmentsNotMentionedInRequest(List<NetworkAttachment> networkAttachments) {
        for (VdsNetworkInterface nic : nicsByName.values()) {
            String networkName = nic.getNetworkName();
            if (networkName != null && clusterNetworks.containsKey(networkName)) {

                NetworkAttachment networkAttachmentRelatedToNetwork =
                    getNetworkAttachmentRelatedToNetwork(networkAttachments, clusterNetworks.get(networkName));
                boolean networkAttachmentRelatedToNetworkExist = networkAttachmentRelatedToNetwork != null;

                if (networkAttachmentRelatedToNetworkExist) {
                    VdsNetworkInterface baseInterfaceNicOrThis = getBaseInterfaceNicOrThis(nic);
                    boolean shouldUpdateExistingAttachment =
                        !baseInterfaceNicOrThis.getId().equals(networkAttachmentRelatedToNetwork.getNicId());

                    if (shouldUpdateExistingAttachment) {
                        networkAttachmentRelatedToNetwork.setNicId(baseInterfaceNicOrThis.getId());
                        networkAttachmentRelatedToNetwork.setNicName(baseInterfaceNicOrThis.getName());
                        networkAttachmentDao.update(networkAttachmentRelatedToNetwork);
                    }

                } else {
                    if (!nic.isPartOfBond()) {
                        createNetworkAttachmentForReportedNetworksNotHavingOne(nic, networkName);
                    }
                }
            }
        }
    }

    private void createNetworkAttachmentForReportedNetworksNotHavingOne(VdsNetworkInterface nic, String networkName) {
        NetworkAttachment networkAttachment = new NetworkAttachment();
        networkAttachment.setId(Guid.newGuid());
        networkAttachment.setNetworkId(clusterNetworks.get(networkName).getId());
        Guid nicId = getBaseInterfaceNicOrThis(nic).getId();

        networkAttachment.setNicId(nicId);
        networkAttachment.setProperties(nic.getCustomProperties());
        networkAttachment.setIpConfiguration(createIpConfigurationFromVdsNetworkInterface(nic));

        networkAttachmentDao.save(networkAttachment);
    }

    private VdsNetworkInterface getBaseInterfaceNicOrThis(VdsNetworkInterface nic) {
        return nic.getBaseInterface() == null ? nic : nicsByName.get(nic.getBaseInterface());
    }

    private IpConfiguration createIpConfigurationFromVdsNetworkInterface(VdsNetworkInterface nic) {
        IPv4Address iPv4Address = new IPv4Address();
        iPv4Address.setAddress(nic.getAddress());
        iPv4Address.setNetmask(nic.getSubnet());
        iPv4Address.setGateway(nic.getGateway());

        IpConfiguration ipConfiguration = new IpConfiguration();
        ipConfiguration.setBootProtocol(nic.getBootProtocol());
        ipConfiguration.setIPv4Addresses(Collections.singletonList(iPv4Address));

        return ipConfiguration;
    }

    private NetworkAttachment getNetworkAttachmentRelatedToNetwork(List<NetworkAttachment> networkAttachments,
        final Network network) {
        return LinqUtils.firstOrNull(networkAttachments,
            new Predicate<NetworkAttachment>() {
                @Override
                public boolean eval(NetworkAttachment networkAttachment) {
                    return network.getId().equals(networkAttachment.getNetworkId());
                }
            });
    }

    /**
     * User provided network attachments, which relates to network configured on host, are inserted or updated
     * into database and used to update passed in <code>networkAttachments</code> collection.
     */
    private void persistOrUpdateUserNetworkAttachments(List<NetworkAttachment> networkAttachments) {
        Map<Guid, NetworkAttachment> validDbAttachmentsById = Entities.businessEntitiesById(networkAttachments);
        for (NetworkAttachment networkAttachment : userNetworkAttachments) {
            if (networkConfiguredOnHost(networkAttachment.getNetworkId())) {
                VdsNetworkInterface reportedNicToWhichIsNetworkAttached =
                    reportedNicsByNetworkId.get(networkAttachment.getNetworkId());

                Guid reportedNicId = baseInterfaceOfNic(reportedNicToWhichIsNetworkAttached).getId();
                if (!networkAttachment.getNicId().equals(reportedNicId)) {
                    throw new IllegalStateException(
                        INCONSISTENCY_NETWORK_IS_REPORTED_ON_DIFFERENT_NIC_THAN_WAS_SPECIFIED);
                }

                boolean alreadyExistingAttachment = validDbAttachmentsById.containsKey(networkAttachment.getId());
                if (alreadyExistingAttachment) {
                    networkAttachmentDao.update(networkAttachment);
                    networkAttachments.remove(validDbAttachmentsById.get(networkAttachment.getId()));
                    networkAttachments.add(networkAttachment);
                } else {
                    networkAttachment.setId(Guid.newGuid());
                    networkAttachmentDao.save(networkAttachment);
                    networkAttachments.add(networkAttachment);
                }
            }
        }
    }

    private VdsNetworkInterface baseInterfaceOfNic(VdsNetworkInterface nic) {
        return nicsByName.get(NetworkUtils.stripVlan(nic));
    }

    /**
     * Removes all {@link org.ovirt.engine.core.common.businessentities.network.NetworkAttachment NetworkAttachments},
     * which network aren't associated with any of given {@link #nicsByName nics}, both from
     * <code>existingNetworkAttachments</code> and from db.
     *
     * @param existingNetworkAttachments all existing attachments.
     */
    private void removeInvalidNetworkAttachmentsFromDb(List<NetworkAttachment> existingNetworkAttachments) {
        for (Iterator<NetworkAttachment> iterator = existingNetworkAttachments.iterator(); iterator.hasNext(); ) {
            NetworkAttachment networkAttachment = iterator.next();
            boolean attachmentRelatesToNetworkNotReportedOnHost =
                !networkConfiguredOnHost(networkAttachment.getNetworkId());

            if (attachmentRelatesToNetworkNotReportedOnHost) {
                networkAttachmentDao.remove(networkAttachment.getId());
                iterator.remove();
            }
        }
    }

    /**
     * @param networkId network ID.
     * @return true if given network ID describes network bound to any reported NIC.
     */
    private boolean networkConfiguredOnHost(Guid networkId) {
        return reportedNetworksById.containsKey(networkId);
    }

}
