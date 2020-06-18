package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class VfsConfigModel extends EntityModel<HostNicVfsConfig> {
    private EntityModel<Integer> maxNumOfVfs = new EntityModel<>();
    private EntityModel<Integer> numOfVfs = new EntityModel<>();
    private ListModel<AllNetworksSelector> allNetworksAllowed = new ListModel<>();
    private ListModel<VfsConfigNetwork> networks = new ListModel<>();
    private VfsNicLabelModel labelsModel;

    public VfsConfigModel() {
        this(new HostNicVfsConfig(), Collections.emptyList(), new TreeSet<>());
    }

    public VfsConfigModel(HostNicVfsConfig vfsConfig, List<Network> allClusterNetworks, SortedSet<String> dcLabels) {
        setEntity(vfsConfig);
        maxNumOfVfs.setEntity(vfsConfig.getMaxNumOfVfs());
        numOfVfs.setEntity(vfsConfig.getNumOfVfs());
        allNetworksAllowed.setItems(Arrays.asList(AllNetworksSelector.values()));
        allNetworksAllowed.setSelectedItem(vfsConfig.isAllNetworksAllowed() ? AllNetworksSelector.allNetworkAllowed
                : AllNetworksSelector.specificNetworks);

        labelsModel = new VfsNicLabelModel(new ArrayList<>(vfsConfig.getNetworkLabels()), dcLabels);
        initNetworks(allClusterNetworks);
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

    public VfsNicLabelModel getLabelsModel() {
        return labelsModel;
    }

    public void setLabelsModel(VfsNicLabelModel labels) {
        this.labelsModel = labels;
    }

    public EntityModel<Integer> getMaxNumOfVfs() {
        return maxNumOfVfs;
    }

    private void initNetworks(List<Network> allClusterNetworks) {
        List<VfsConfigNetwork> vfsConfigNetworks = new ArrayList<>();

        Set<Guid> attachedNetworks = getEntity().getNetworks();
        for (Network network : nonExternalNetworks(allClusterNetworks)) {
            boolean isAttached = attachedNetworks.contains(network.getId());
            VfsConfigNetwork vfsConfigNetwork =
                    new VfsConfigNetwork(isAttached, labelsModel, network);
            vfsConfigNetworks.add(vfsConfigNetwork);
        }

        networks.setItems(vfsConfigNetworks);
    }

    private List<Network> nonExternalNetworks(List<Network> networks) {
        return networks.stream().filter(network -> !network.isExternal()).collect(Collectors.toList());
    }

    public boolean validate() {
        numOfVfs.validateEntity(new IValidation[] { new NotEmptyValidation(),
                new IntegerValidation(0, getMaxNumOfVfs().getEntity()) });

        labelsModel.validate();

        return labelsModel.getIsValid() && numOfVfs.getIsValid();
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
