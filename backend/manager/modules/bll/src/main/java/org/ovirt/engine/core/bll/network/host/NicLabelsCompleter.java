package org.ovirt.engine.core.bll.network.host;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.common.action.HostSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NicLabel;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.utils.MapNetworkAttachments;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.NetworkUtils;

public class NicLabelsCompleter {

    private HostSetupNetworksParameters params;
    private Map<String, List<Network>> labelToNetworks;
    private Map<Guid, NetworkAttachment> attachmentsByNetworkId;
    private Map<Guid, NetworkAttachment> existingNetworkAttachmentsByNetworkId;
    private List<Network> clusterNetworks;
    private BusinessEntityMap<VdsNetworkInterface> existingNicsBusinessEntityMap;

    public NicLabelsCompleter(HostSetupNetworksParameters params,
            List<NetworkAttachment> existingNetworkAttachments,
            List<Network> clusterNetworks,
            BusinessEntityMap<VdsNetworkInterface> existingNicsBusinessEntityMap) {
        this.labelToNetworks = new HashMap<>();
        this.params = params;
        this.clusterNetworks = clusterNetworks;

        this.attachmentsByNetworkId = new MapNetworkAttachments(params.getNetworkAttachments()).byNetworkId();

        this.existingNetworkAttachmentsByNetworkId =
                new MapNetworkAttachments(existingNetworkAttachments).byNetworkId();

        this.existingNicsBusinessEntityMap = existingNicsBusinessEntityMap;

        initLabelsToNetworks();
    }

    /**
     * The method updates <code>networkAttachments</code> and <code>removedNetworkAttachments</code> according to
     * <code>labels</code> and <code>removedLabels</code>.
     *
     * <code>labels</code> can contain two types-
     * <ol>
     * <li>labels that are already attached to a nic on the host.</li>
     * <li>new labels.</li>
     * </ol>
     * <br>
     *
     * For the first type, the existing attachment is modified to contain the new nic (and added to <code>networkAttachments</code>).
     * For the second type, a new network attachment is created (and added to <code>networkAttachments</code> ).
     *
     * An attachment is added to the <code>removedNetworkAttachments</code> in case the network was attached to the nic
     * via one of the <code>removedLabels</code>.
     */
    public void completeNetworkAttachments() {
        completeNetworkAttachmentsByLabels();
        completeNetworkAttachmentsByRemovedLabels();
    }

    private void initLabelsToNetworks() {
        for (Network network : clusterNetworks) {
            labelToNetworks.computeIfAbsent(network.getLabel(), k -> new ArrayList<>()).add(network);
        }
    }

    private void completeNetworkAttachmentsByRemovedLabels() {
        for (String removedLabel : params.getRemovedLabels()) {
            List<Network> labelNetworks = labelToNetworks.get(removedLabel);

            if (labelNetworks == null) {
                continue;
            }

            for (Network network : labelNetworks) {
                NetworkAttachment existingNetworkAttachment =
                        existingNetworkAttachmentsByNetworkId.get(network.getId());

                if (shouldRemoveExistingAttachment(existingNetworkAttachment, removedLabel)) {
                    params.getRemovedNetworkAttachments().add(existingNetworkAttachment.getId());
                }
            }
        }
    }

    private boolean shouldRemoveExistingAttachment(NetworkAttachment existingAttachmentWithLabeledNetwork,
            String removedLabel) {
        if (existingAttachmentWithLabeledNetwork == null) {
            return false;
        }

        NetworkAttachment newOrModifiedNetworkAttachment =
                attachmentsByNetworkId.get(existingAttachmentWithLabeledNetwork.getNetworkId());
        boolean attachmentWasUpdated =
                newOrModifiedNetworkAttachment != null
                        && existingAttachmentWithLabeledNetwork.getId().equals(newOrModifiedNetworkAttachment.getId());

        if (!attachmentWasUpdated) {
            return containsLabel(existingAttachmentWithLabeledNetwork.getNicId(), removedLabel);
        }

        return false;
    }

    private boolean containsLabel(Guid nicId, String label) {
        VdsNetworkInterface attachmentNic = existingNicsBusinessEntityMap.get(nicId);

        return NetworkUtils.isLabeled(attachmentNic) && attachmentNic.getLabels().contains(label);
    }

    private void completeNetworkAttachmentsByLabels() {
        for (NicLabel nicLabel : params.getLabels()) {
            List<Network> labelNetworks = labelToNetworks.get(nicLabel.getLabel());

            if (labelNetworks == null) {
                continue;
            }

            for (Network network : labelNetworks) {
                NetworkAttachment newOrModifiedNetworkAttachment = attachmentsByNetworkId.get(network.getId());
                NetworkAttachment existingNetworkAttachment =
                        existingNetworkAttachmentsByNetworkId.get(network.getId());
                boolean existingAttachmentRemoved =
                        existingNetworkAttachment == null ? false : params.getRemovedNetworkAttachments()
                                .contains(existingNetworkAttachment.getId());

                boolean noNewOrModifiedNetworkAttachment = newOrModifiedNetworkAttachment == null;
                NetworkAttachment attachmentToConfigure =
                        noNewOrModifiedNetworkAttachment && !existingAttachmentRemoved ? existingNetworkAttachment
                                : newOrModifiedNetworkAttachment;
                if (attachmentToConfigure == null) {
                    params.getNetworkAttachments().add(createNetworkAttachment(nicLabel.getNicId(),
                            nicLabel.getNicName(),
                            network.getId(),
                            network.getName()));
                } else if (!Objects.equals(attachmentToConfigure.getNicName(), nicLabel.getNicName())
                        && noNewOrModifiedNetworkAttachment) {
                    NetworkAttachment updatedNetworkAttachment = new NetworkAttachment(existingNetworkAttachment);
                    updatedNetworkAttachment.setNicId(nicLabel.getNicId());
                    updatedNetworkAttachment.setNicName(nicLabel.getNicName());
                    params.getNetworkAttachments().add(updatedNetworkAttachment);
                }
            }
        }
    }

    private NetworkAttachment createNetworkAttachment(Guid nicId, String nicName, Guid networkId, String networkName) {
        NetworkAttachment networkAttachment = new NetworkAttachment();

        networkAttachment.setNicId(nicId);
        networkAttachment.setNicName(nicName);
        networkAttachment.setNetworkId(networkId);
        networkAttachment.setNetworkName(networkName);
        IpConfiguration ipConfiguration = NetworkCommonUtils.createDefaultIpConfiguration();
        networkAttachment.setIpConfiguration(ipConfiguration);

        return networkAttachment;
    }
}
