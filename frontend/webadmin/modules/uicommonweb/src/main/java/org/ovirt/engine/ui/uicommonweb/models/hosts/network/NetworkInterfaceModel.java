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

    public NetworkInterfaceModel(HostSetupNetworksModel setupModel) {
        super(setupModel);
        List<LogicalNetworkModel> networks = new ArrayList<>();
        setItems(networks);
        labels = new ArrayList<>();
    }

    public NetworkInterfaceModel(VdsNetworkInterface nic,
            Collection<LogicalNetworkModel> nicNetworks,
            Collection<NetworkLabelModel> nicLabels,
            HostSetupNetworksModel setupModel) {
        this(nic, setupModel);
        // attach all networks
        for (LogicalNetworkModel network : nicNetworks) {
            network.attach(this, false);
        }
        if (nicLabels != null) {
            labels.addAll(nicLabels);
        }
    }

    public NetworkInterfaceModel(VdsNetworkInterface nic, HostSetupNetworksModel setupModel) {
        this(setupModel);
        setIface(nic);
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

    private String culpritNetwork;

    /**
     * If this NIC was the destination of a null bond operation, the culprit network is one of those that caused the
     * operation to fail, the first encountered of the following: unmanaged, out of sync, one of several non-VLAN
     * networks, VM network when VLAN networks exist.
     *
     * @return the name of the network at fault, or null if there isn't one.
     */
    public String getCulpritNetwork() {
        return culpritNetwork;
    }

    public void setCulpritNetwork(String culpritNetwork) {
        this.culpritNetwork = culpritNetwork;
    }
}
