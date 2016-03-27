package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    private String physicalFunction;

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
            String physicalFunction,
            HostSetupNetworksModel setupModel) {
        this(nic, sriovEnabled, physicalFunction, setupModel);

        // attach all networks
        if (nicNetworks != null) {
            for (LogicalNetworkModel logicalNetworkModel : nicNetworks) {
                logicalNetworkModel.attach(this);
            }
        }

        // add all labels
        if (nicLabels != null) {
            for (NetworkLabelModel label : nicLabels) {
                label(label);
            }
        }
    }

    public NetworkInterfaceModel(VdsNetworkInterface iface,
            boolean sriovEnabled,
            String physicalFunction,
            HostSetupNetworksModel setupModel) {
        this(setupModel);
        this.iface = iface;
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
    }

    public void unlabel(NetworkLabelModel labelModel) {
        labelModel.setInterface(null);
        getLabels().remove(labelModel);
    }

    public int getTotalItemSize() {
        return getItems().size() + labels.size();
    }

    public VdsNetworkInterface getOriginalIface() {
        return iface;
    }

    @Override
    public String getName() {
        return getOriginalIface().getName();
    }

    @Override
    public InterfaceStatus getStatus() {
        return getOriginalIface().getStatistics().getStatus();
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

    public String getPhysicalFunction() {
        return physicalFunction;
    }
}
