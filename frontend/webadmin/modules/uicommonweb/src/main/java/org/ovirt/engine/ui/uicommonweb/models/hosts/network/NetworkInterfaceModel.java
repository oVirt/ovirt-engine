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

    public NetworkInterfaceModel(HostSetupNetworksModel setupModel) {
        super(setupModel);
        List<LogicalNetworkModel> networks = new ArrayList<LogicalNetworkModel>();
        setItems(networks);
    }

    public NetworkInterfaceModel(VdsNetworkInterface nic,
            Collection<LogicalNetworkModel> nicNetworks,
            HostSetupNetworksModel setupModel) {
        this(nic, setupModel);
        // attach all networks
        for (LogicalNetworkModel network : nicNetworks) {
            network.attach(this, false);
        }
    }

    public NetworkInterfaceModel(VdsNetworkInterface nic, HostSetupNetworksModel setupModel) {
        this(setupModel);
        setEntity(nic);
    }

    public BondNetworkInterfaceModel getBond() {
        return bond;
    }

    @Override
    public VdsNetworkInterface getEntity() {
        return (VdsNetworkInterface) super.getEntity();
    }

    @Override
    public List<LogicalNetworkModel> getItems() {
        return (List<LogicalNetworkModel>) super.getItems();
    }

    @Override
    public String getName() {
        return getEntity().getName();
    }

    @Override
    public InterfaceStatus getStatus() {
        return getEntity().getStatistics().getStatus();
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
}
