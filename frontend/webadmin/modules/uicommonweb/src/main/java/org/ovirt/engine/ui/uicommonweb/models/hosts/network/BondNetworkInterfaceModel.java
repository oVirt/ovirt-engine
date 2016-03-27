package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.action.CreateOrUpdateBond;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostSetupNetworksModel;

/**
 * A Model for Bond Interfaces
 */
public class BondNetworkInterfaceModel extends NetworkInterfaceModel {

    private final List<NetworkInterfaceModel> slaves;
    private final CreateOrUpdateBond createOrUpdateBond;

    public BondNetworkInterfaceModel(Bond bondOriginalNic,
            CreateOrUpdateBond createOrUpdateBond,
            Collection<LogicalNetworkModel> nicNetworks,
            Collection<NetworkLabelModel> nicLabels,
            List<NetworkInterfaceModel> slaves,
            HostSetupNetworksModel setupModel) {
        super(bondOriginalNic, nicNetworks, nicLabels, false, null, setupModel);
        this.createOrUpdateBond = createOrUpdateBond;
        this.slaves = slaves;

        setThisBondModelToRelatedSlavesModels(slaves);
    }

    public void setThisBondModelToRelatedSlavesModels(List<NetworkInterfaceModel> slaveModels) {
        for (NetworkInterfaceModel slaveModel : slaveModels) {
            slaveModel.setBond(this);
        }
    }

    public List<NetworkInterfaceModel> getSlaves() {
        return slaves;
    }

    @Override
    public String getName() {
        return createOrUpdateBond.getName();
    }

    public String getBondOptions() {
        return getCreateOrUpdateBond().getBondOptions();
    }

    @Override
    public Bond getOriginalIface() {
        return (Bond) super.getOriginalIface();
    }

    public CreateOrUpdateBond getCreateOrUpdateBond() {
        return createOrUpdateBond;
    }
}
