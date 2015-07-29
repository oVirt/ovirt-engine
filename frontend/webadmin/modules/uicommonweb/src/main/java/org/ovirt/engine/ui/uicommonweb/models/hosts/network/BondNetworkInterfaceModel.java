package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostSetupNetworksModel;

/**
 * A Model for Bond Interfaces
 */
public class BondNetworkInterfaceModel extends NetworkInterfaceModel {

    private final List<NetworkInterfaceModel> bonded;

    public BondNetworkInterfaceModel(Bond bondNic,
            Collection<LogicalNetworkModel> nicNetworks,
            Collection<NetworkLabelModel> nicLabels,
            List<NetworkInterfaceModel> bonded, HostSetupNetworksModel setupModel) {
        super(bondNic, nicNetworks, nicLabels, false, setupModel);
        this.bonded = bonded;

        setThisBondModelToRelatedSlavesModels(bonded);
    }

    public void setThisBondModelToRelatedSlavesModels(List<NetworkInterfaceModel> slaveModels) {
        for (NetworkInterfaceModel slaveModel : slaveModels) {
            slaveModel.setBond(this);
        }
    }

    public List<NetworkInterfaceModel> getBonded() {
        return bonded;
    }

    /**
     * Break the Bond
     */
    public void breakBond() {
        // remove bond name
        for (NetworkInterfaceModel bonded : getBonded()) {
            bonded.getIface().setBondName(null);
        }
    }

    public String getBondOptions() {
        return getIface().getBondOptions();
    }

    @Override
    public Bond getIface() {
        return (Bond) super.getIface();
    }
}
