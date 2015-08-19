package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NicLabel;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

public class DataFromHostSetupNetworksModel {
    public List<VdsNetworkInterface> allNics;
    public List<NetworkAttachment> existingNetworkAttachments;
    public Set<String> networksToSync;

    public Set<NicLabel> addedLabels = new HashSet<>();
    public Set<NicLabel> removedLabels = new HashSet<>();

    public List<NetworkAttachment> newOrModifiedNetworkAttachments = new ArrayList<>();
    public List<NetworkAttachment> removedNetworkAttachments = new ArrayList<>();
    public List<Bond> newOrModifiedBonds = new ArrayList<>();
    public List<Bond> removedBonds = new ArrayList<>();
    public Set<String> removedUnmanagedNetworks = new HashSet<>();

    public DataFromHostSetupNetworksModel() {
    }

    public DataFromHostSetupNetworksModel(List<VdsNetworkInterface> allNics,
            List<NetworkAttachment> existingNetworkAttachments, Set<String> networksToSync) {
        this.allNics = allNics;
        this.existingNetworkAttachments = existingNetworkAttachments;
        this.networksToSync = networksToSync;
    }
}
