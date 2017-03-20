package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import java.util.HashSet;
import java.util.Map;
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

    private Set<String> networksToSync = new HashSet<>();

    private Set<String> removedUnmanagedNetworks = new HashSet<>();

    private Map<Guid, Guid> networkIdToExistingAttachmentId;

    private Map<String, CreateOrUpdateBond> originalBondsByName;

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

    public void addNetworkAttachmentToParameters(NetworkAttachment networkAttachment) {
        assert networkAttachment.getId() == null : "When adding attachment to parameters its id should be null"; //$NON-NLS-1$
        Guid idOfAttachmentNetworkWasPreviouslyAttachedTo =
                networkIdToExistingAttachmentId.get(networkAttachment.getNetworkId());
        networkAttachment.setId(idOfAttachmentNetworkWasPreviouslyAttachedTo);

        if (idOfAttachmentNetworkWasPreviouslyAttachedTo != null) {
            removedNetworkAttachments.remove(idOfAttachmentNetworkWasPreviouslyAttachedTo);
        }

        networkAttachments.add(networkAttachment);
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

    public void addBondToParameters(CreateOrUpdateBond bond) {
        if (bond.getId() != null) {
            throw new IllegalArgumentException("When adding a bond to the parameters its id should be null"); //$NON-NLS-1$
        }

        CreateOrUpdateBond originalBondWithTheSameName =
                originalBondsByName.get(bond.getName());

        if (originalBondWithTheSameName != null) {
            bond.setId(originalBondWithTheSameName.getId());
            removedBonds.remove(originalBondWithTheSameName.getId());
        }

        bonds.add(bond);
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

    public void setNetworkIdToExistingAttachmentId(Map<Guid, Guid> networkIdToExistingAttachmentId) {
        this.networkIdToExistingAttachmentId = networkIdToExistingAttachmentId;
    }

    public void setOriginalBondsByName(Map<String, CreateOrUpdateBond> originalBondsByName) {
        this.originalBondsByName = originalBondsByName;
    }
}
