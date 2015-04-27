package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
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

public class VfsConfigModel extends EntityModel<HostNicVfsConfig> {
    private EntityModel<Integer> maxNumOfVfs = new EntityModel<>();
    private EntityModel<Integer> numOfVfs = new EntityModel<>();
    private EntityModel<Boolean> allNetworksAllowed = new EntityModel<>();
    private ListModel<Network> networks = new ListModel<>();
    private ListModel<String> labels = new ListModel<>();

    public VfsConfigModel() {
        this(new HostNicVfsConfig(), new ArrayList<Network>());
    }

    public VfsConfigModel(HostNicVfsConfig vfsConfig, List<Network> allClusterNetworks) {
        setEntity(vfsConfig);
        maxNumOfVfs.setEntity(vfsConfig.getMaxNumOfVfs());
        numOfVfs.setEntity(vfsConfig.getNumOfVfs());
        allNetworksAllowed.setEntity(vfsConfig.isAllNetworksAllowed());
        initNetworks(allClusterNetworks);
        labels.setItems(vfsConfig.getNetworkLabels());
    }

    public EntityModel<Integer> getNumOfVfs() {
        return numOfVfs;
    }

    public void setNumOfVfs(EntityModel<Integer> numOfVfs) {
        this.numOfVfs = numOfVfs;
    }

    public EntityModel<Boolean> getAllNetworksAllowed() {
        return allNetworksAllowed;
    }

    public void setAllNetworksAllowed(EntityModel<Boolean> allNetworksAllowed) {
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
}
