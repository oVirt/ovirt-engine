package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.common.action.CreateOrUpdateBond;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NicLabel;
import org.ovirt.engine.core.compat.Guid;

public class DataFromHostSetupNetworksModel {
    private Set<NicLabel> labels = new HashSet<>();
    private Set<String> removedLabels = new HashSet<>();
    private Set<String> originalLabels = new HashSet<>();

    private Set<NetworkAttachment> networkAttachments = new HashSet<>();
    private Set<Guid> removedNetworkAttachments = new HashSet<>();

    private Set<CreateOrUpdateBond> bonds = new HashSet<>();
    private Set<Guid> removedBonds = new HashSet<>();

    private Set<String> networksToSync = new HashSet<>();;

    private Set<String> removedUnmanagedNetworks = new HashSet<>();

    public Set<String> getNetworksToSync() {
        return networksToSync;
    }

    public Set<NicLabel> getLabels() {
        return labels;
    }

    public Set<String> getRemovedLabels() {
        return removedLabels;
    }

    public Set<String> getOriginalLabels() {
        return originalLabels;
    }

    public Set<NetworkAttachment> getNetworkAttachments() {
        return networkAttachments;
    }

    public Set<Guid> getRemovedNetworkAttachments() {
        return removedNetworkAttachments;
    }

    public Set<CreateOrUpdateBond> getBonds() {
        return bonds;
    }

    public Set<Guid> getRemovedBonds() {
        return removedBonds;
    }

    public Set<String> getRemovedUnmanagedNetworks() {
        return removedUnmanagedNetworks;
    }

    public void removeNetworkAttachmentFromParameters(NetworkAttachment networkAttachment) {
        networkAttachments.remove(networkAttachment);

        if (networkAttachment.getId() != null) {
            removedNetworkAttachments.add(networkAttachment.getId());
        }
    }

    public void removeBondFromParameters(CreateOrUpdateBond bond) {
        bonds.remove(bond);

        if (bond.getId() != null) {
            removedBonds.add(bond.getId());
        }
    }

    public void removeLabelFromParameters(NicLabel nicLabel) {
        labels.remove(nicLabel);

        if (originalLabels.contains(nicLabel.getLabel())) {
            removedLabels.add(nicLabel.getLabel());
        }
    }

    public void addLabelToParameters(NicLabel nicLabel) {
        labels.add(nicLabel);

        if (originalLabels.contains(nicLabel.getLabel())) {
            removedLabels.remove(nicLabel.getLabel());
        }
    }
}
