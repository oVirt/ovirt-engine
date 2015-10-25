package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostSetupNetworksModel;

/**
 * A Model for Network Interface Controllers (NICs)
 */
public class NetworkInterfaceModel extends NetworkItemModel<InterfaceStatus> {

    private boolean bonded = false;
    private BondNetworkInterfaceModel bond;
    private List<NetworkLabelModel> labels;
    private VdsNetworkInterface iface;
    private boolean sriovEnabled = false;
    private VdsNetworkInterface physicalFunction;

    public NetworkInterfaceModel(HostSetupNetworksModel setupModel) {
        super(setupModel);
        List<LogicalNetworkModel> networks = new ArrayList<>();
        setItems(networks);
        labels = new ArrayList<>();
    }

    public NetworkInterfaceModel(VdsNetworkInterface nic,
            Collection<LogicalNetworkModel> nicNetworks,
            Collection<NetworkLabelModel> nicLabels,
            boolean sriovEnabled,
            VdsNetworkInterface physicalFunction,
            HostSetupNetworksModel setupModel) {
        this(nic, sriovEnabled, physicalFunction, setupModel);

        // attach all networks
        for (LogicalNetworkModel network : nicNetworks) {
            network.attach(this, false);
        }

        // add all labels
        if (nicLabels != null) {
            for (NetworkLabelModel label : nicLabels) {
                label(label);
            }
        }
    }

    public NetworkInterfaceModel(VdsNetworkInterface nic,
            boolean sriovEnabled,
            VdsNetworkInterface physicalFunction,
            HostSetupNetworksModel setupModel) {
        this(setupModel);
        setIface(nic);
        this.sriovEnabled = sriovEnabled;
        this.physicalFunction = physicalFunction;
    }

    public BondNetworkInterfaceModel getBond() {
        return bond;
    }

    @Override
    public List<LogicalNetworkModel> getItems() {
        return (List<LogicalNetworkModel>) super.getItems();
    }

    public List<NetworkLabelModel> getLabels() {
        return labels;
    }

    public void label(NetworkLabelModel labelModel) {
        labelModel.setInterface(this);
        getLabels().add(labelModel);

        Set<String> labels = getIface().getLabels();
        if (labels == null) {
            labels = new HashSet<>();
            getIface().setLabels(labels);
        }
        labels.add(labelModel.getName());
    }

    public void unlabel(NetworkLabelModel labelModel) {
        labelModel.setInterface(null);
        getLabels().remove(labelModel);

        Set<String> labels = getIface().getLabels();
        labels.remove(labelModel.getName());
        if (labels.isEmpty()) {
            getIface().setLabels(null);
        }
    }

    public int getTotalItemSize() {
        return getItems().size() + labels.size();
    }

    public VdsNetworkInterface getIface() {
        return iface;
    }

    public void setIface(VdsNetworkInterface iface) {
        this.iface = iface;
    }

    @Override
    public String getName() {
        return getIface().getName();
    }

    @Override
    public InterfaceStatus getStatus() {
        return getIface().getStatistics().getStatus();
    }

    public boolean isBonded() {
        return bonded;
    }

    public void setBond(BondNetworkInterfaceModel bond) {
        this.bond = bond;
    }

    public void setBonded(boolean bonded) {
        this.bonded = bonded;
    }

    @Override
    public String getType() {
        return HostSetupNetworksModel.NIC;
    }

    @Override
    public boolean aggregatesNetworks() {
        return true;
    }

    public boolean isSriovEnabled() {
        return sriovEnabled;
    }

    public boolean isVf() {
        return physicalFunction != null;
    }

    public VdsNetworkInterface getPhysicalFunction() {
        return physicalFunction;
    }
}
