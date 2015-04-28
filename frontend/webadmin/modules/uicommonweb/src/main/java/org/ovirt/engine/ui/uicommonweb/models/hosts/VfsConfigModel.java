package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class VfsConfigModel extends EntityModel<HostNicVfsConfig> {
    private EntityModel<Integer> maxNumOfVfs = new EntityModel<>();
    private EntityModel<Integer> numOfVfs = new EntityModel<>();
    private ListModel<AllNetworksSelector> allNetworksAllowed = new ListModel<>();
    private ListModel<VfsConfigNetwork> networks = new ListModel<>();
    private ListModel<String> labels = new ListModel<>();

    public VfsConfigModel() {
        this(new HostNicVfsConfig(), new ArrayList<Network>());
    }

    public VfsConfigModel(HostNicVfsConfig vfsConfig, List<Network> allClusterNetworks) {
        setEntity(vfsConfig);
        maxNumOfVfs.setEntity(vfsConfig.getMaxNumOfVfs());
        numOfVfs.setEntity(vfsConfig.getNumOfVfs());
        allNetworksAllowed.setItems(Arrays.asList(AllNetworksSelector.values()));
        allNetworksAllowed.setSelectedItem(vfsConfig.isAllNetworksAllowed() ? AllNetworksSelector.allNetworkAllowed
                : AllNetworksSelector.specificNetworks);
        initNetworks(allClusterNetworks);
        labels.setItems(vfsConfig.getNetworkLabels());
    }

    public EntityModel<Integer> getNumOfVfs() {
        return numOfVfs;
    }

    public void setNumOfVfs(EntityModel<Integer> numOfVfs) {
        this.numOfVfs = numOfVfs;
    }

    public ListModel<AllNetworksSelector> getAllNetworksAllowed() {
        return allNetworksAllowed;
    }

    public void setAllNetworksAllowed(ListModel<AllNetworksSelector> allNetworksAllowed) {
        this.allNetworksAllowed = allNetworksAllowed;
    }

    public ListModel<VfsConfigNetwork> getNetworks() {
        return networks;
    }

    public void setNetworks(ListModel<VfsConfigNetwork> networks) {
        this.networks = networks;
    }

    public ListModel<String> getLabels() {
        return labels;
    }

    public void setLabels(ListModel<String> labels) {
        this.labels = labels;
    }

    public EntityModel<Integer> getMaxNumOfVfs() {
        return maxNumOfVfs;
    }

    private void initNetworks(List<Network> allClusterNetworks) {
        List<VfsConfigNetwork> vfsConfigNetworks = new ArrayList<>();

        Set<Guid> attachedNetworks = getEntity().getNetworks();
        for (Network network : allClusterNetworks) {
            boolean isAttached = attachedNetworks.contains(network.getId());
            VfsConfigNetwork vfsConfigNetwork =
                    new VfsConfigNetwork(isAttached, isAttached ? getAttachedViaLabel(network) : null, network);
            vfsConfigNetworks.add(vfsConfigNetwork);
        }

        networks.setItems(vfsConfigNetworks);
    }

    private String getAttachedViaLabel(Network network) {
        // TODO return the label just if the network is really attached via the label
        return network.getLabel();
    }

    public static enum AllNetworksSelector {
        allNetworkAllowed(ConstantsManager.getInstance().getConstants().allNetworksAllowed()),
        specificNetworks(ConstantsManager.getInstance().getConstants().specificNetworksAllowed());

        private String description;

        private AllNetworksSelector(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }
}
