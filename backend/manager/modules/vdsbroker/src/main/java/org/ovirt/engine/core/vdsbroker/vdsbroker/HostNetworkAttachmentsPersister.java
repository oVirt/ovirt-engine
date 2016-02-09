package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.common.action.CustomPropertiesForVdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.utils.NetworkUtils;

public class HostNetworkAttachmentsPersister {

    static final String INCONSISTENCY_NETWORK_IS_REPORTED_ON_DIFFERENT_NIC_THAN_WAS_SPECIFIED = "Inconsistency in current state and reported data: given network is reported on different nic than was specified.";
    private final NetworkAttachmentDao networkAttachmentDao;
    private final Guid hostId;
    private final CustomPropertiesForVdsNetworkInterface customPropertiesForNics;
    private final boolean legacyMode;
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

    public HostNetworkAttachmentsPersister(NetworkAttachmentDao networkAttachmentDao,
            Guid hostId,
            List<VdsNetworkInterface> nics,
            List<NetworkAttachment> userNetworkAttachments,
            List<Network> clusterNetworks) {
        this(networkAttachmentDao, hostId, nics, false, null, userNetworkAttachments, clusterNetworks);
    }

    /**
     * This constructors creates {@link HostNetworkAttachmentsPersister} instance, which behaves in a way required by
     * {@code SetupNetworksCommand}, which is planned for demise in 4.0, when this should be removed as well.
     */
    public HostNetworkAttachmentsPersister(NetworkAttachmentDao networkAttachmentDao,
            Guid hostId,
            List<VdsNetworkInterface> nics,
            CustomPropertiesForVdsNetworkInterface customPropertiesForNics,
            List<NetworkAttachment> userNetworkAttachments,
            List<Network> clusterNetworks) {
        this(networkAttachmentDao, hostId, nics, true, customPropertiesForNics, userNetworkAttachments, clusterNetworks);
    }

    private HostNetworkAttachmentsPersister(NetworkAttachmentDao networkAttachmentDao,
            Guid hostId,
            List<VdsNetworkInterface> nics,
            boolean legacyMode,
            CustomPropertiesForVdsNetworkInterface customPropertiesForNics,
            List<NetworkAttachment> userNetworkAttachments,
            List<Network> clusterNetworks) {
        this.networkAttachmentDao = networkAttachmentDao;
        this.hostId = hostId;
        this.customPropertiesForNics = customPropertiesForNics;
        this.legacyMode = legacyMode;
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
                    syncNetworkAttachmentPropertiesWithNic(nic, networkAttachmentRelatedToNetwork);

                } else {
                    if (!nic.isPartOfBond()) {
                        createNetworkAttachmentForReportedNetworksNotHavingOne(nic, networkName);
                    }
                }
            }
        }
    }

    /**
     * @param nic {@link VdsNetworkInterface} representing current state of hosts nic.
     * @param networkAttachment existing network attachment to be updated according to {@code nic} values.
     */
    private void syncNetworkAttachmentPropertiesWithNic(VdsNetworkInterface nic, NetworkAttachment networkAttachment) {
        boolean networkMovedToDifferentNic = updateReferredNic(nic, networkAttachment);
        boolean customPropertiesUpdated = updateCustomProperties(nic, networkAttachment);
        boolean ipConfigurationUpdated = updateIpConfiguration(nic, networkAttachment);

        if (networkMovedToDifferentNic || customPropertiesUpdated || ipConfigurationUpdated) {
            networkAttachmentDao.update(networkAttachment);
        }
    }

    /**
     *
     * @param nic nic to get values from.
     * @param networkAttachment network attachment to update.
     * @return true if nicId and nicName were updated.
     */
    private boolean updateReferredNic(VdsNetworkInterface nic, NetworkAttachment networkAttachment) {
        VdsNetworkInterface baseNic = getBaseInterfaceNicOrThis(nic);
        boolean networkMovedToDifferentNic = !baseNic.getId().equals(networkAttachment.getNicId());

        if (networkMovedToDifferentNic) {
            networkAttachment.setNicId(baseNic.getId());
            networkAttachment.setNicName(baseNic.getName());
        }
        return networkMovedToDifferentNic;
    }

    /**
     *
     * @param nic nic used to get values from.
     * @param networkAttachment network attachment to update.
     * @return true if custom properties were updated.
     */
    private boolean updateCustomProperties(VdsNetworkInterface nic, NetworkAttachment networkAttachment) {
        if (!legacyMode || customPropertiesForNics == null || !customPropertiesForNics.hasCustomPropertiesFor(nic)) {
            return false;
        }

        Map<String, String> networkAttachmentProperties = networkAttachment.getProperties();
        Map<String, String> customPropertiesForNic = customPropertiesForNics.getCustomPropertiesFor(nic);
        boolean doUpdate = !Objects.equals(customPropertiesForNic, networkAttachmentProperties);
        if (doUpdate) {
            networkAttachment.setProperties(customPropertiesForNic);
        }
        return doUpdate;
    }

    /**
     * @param nic nic to get values from.
     * @param networkAttachment network attachment to update.
     * @return true if network attachment was updated.
     */
    private boolean updateIpConfiguration(VdsNetworkInterface nic, NetworkAttachment networkAttachment) {
        if (!legacyMode) {
            return false;
        }

        IpConfiguration nicIpConfiguration = NetworkUtils.createIpConfigurationFromVdsNetworkInterface(nic);
        boolean doUpdate = !Objects.equals(nicIpConfiguration, networkAttachment.getIpConfiguration());
        if (doUpdate) {
            networkAttachment.setIpConfiguration(nicIpConfiguration);
        }
        return doUpdate;
    }

    private void createNetworkAttachmentForReportedNetworksNotHavingOne(VdsNetworkInterface nic, String networkName) {
        NetworkAttachment networkAttachment =
                new NetworkAttachment(getBaseInterfaceNicOrThis(nic),
                        clusterNetworks.get(networkName),
                        NetworkUtils.createIpConfigurationFromVdsNetworkInterface(nic));
        networkAttachment.setId(Guid.newGuid());

        if (legacyMode && customPropertiesForNics != null) {
            networkAttachment.setProperties(customPropertiesForNics.getCustomPropertiesFor(nic));
        }

        networkAttachmentDao.save(networkAttachment);
    }

    private VdsNetworkInterface getBaseInterfaceNicOrThis(VdsNetworkInterface nic) {
        return nic.getBaseInterface() == null ? nic : nicsByName.get(nic.getBaseInterface());
    }

    private NetworkAttachment getNetworkAttachmentRelatedToNetwork(List<NetworkAttachment> networkAttachments,
        final Network network) {
        return networkAttachments.stream()
                .filter(networkAttachment -> network.getId().equals(networkAttachment.getNetworkId()))
                .findFirst()
                .orElse(null);
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

                /*this is needed for attaching network to newly created bond, where network attachment
                does not have nicId set and that nic id is known only after vdsm call.
                * */
                if (networkAttachment.getNicId() == null) {
                    networkAttachment.setNicId(reportedNicId);
                } else {
                    if (!networkAttachment.getNicId().equals(reportedNicId)) {
                        throw new IllegalStateException(
                            INCONSISTENCY_NETWORK_IS_REPORTED_ON_DIFFERENT_NIC_THAN_WAS_SPECIFIED);
                    }
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
