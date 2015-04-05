package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private ListModel<Network> networks = new ListModel<>();
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

    public ListModel<Network> getNetworks() {
        return networks;
    }

    public void setNetworks(ListModel<Network> networks) {
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
        Map<Guid, Network> clusterNetworksMap = createClusterNetworksMap(allClusterNetworks);
        Set<Network> vfsConfigNetworks = new HashSet<>();

        for (Guid networkGuid : getEntity().getNetworks()) {
            vfsConfigNetworks.add(clusterNetworksMap.get(networkGuid));
        }

        networks.setItems(vfsConfigNetworks);
    }

    private Map<Guid, Network> createClusterNetworksMap(List<Network> allClusterNetworks) {
        Map<Guid, Network> clusterNetworksMap = new HashMap<>();

        for (Network network : allClusterNetworks) {
            clusterNetworksMap.put(network.getId(), network);
        }

        return clusterNetworksMap;
    }

    public enum AllNetworksSelector {
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
