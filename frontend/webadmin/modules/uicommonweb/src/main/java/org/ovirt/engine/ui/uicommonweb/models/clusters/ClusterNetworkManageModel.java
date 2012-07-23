package org.ovirt.engine.ui.uicommonweb.models.clusters;

import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.network_cluster;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceListModel;

public class ClusterNetworkManageModel extends EntityModel {

    private boolean attached;
    private boolean vmNetwork;
    private boolean management;

    public ClusterNetworkManageModel(Network network) {
        setEntity(network);
        if (network.getCluster() == null){
            // Init with default values
            getEntity().setCluster(new network_cluster());
        }
        if (HostInterfaceListModel.ENGINE_NETWORK_NAME.equals(network.getname())) {
            setManagement(true);
        }
    }

    @Override
    public Network getEntity() {
        return (Network) super.getEntity();
    }

    public String getName() {
        return getEntity().getname();
    }

    public boolean isAttached() {
        return attached;
    }

    public boolean isDisplayNetwork() {
        return getEntity().getCluster().getis_display();
    }

    public boolean isManagement() {
        return management;
    }

    public boolean isRequired() {
        return getEntity().getCluster().isRequired();
    }

    public boolean isVmNetwork() {
        return vmNetwork;
    }

    public void setAttached(boolean attached) {
        this.attached = attached;
    }

    public void setDisplayNetwork(boolean displayNetwork) {
        getEntity().getCluster().setis_display(displayNetwork);
    }

    public void setManagement(boolean management) {
        this.management = management;
    }

    public void setRequired(boolean required) {
        getEntity().getCluster().setRequired(required);
    }

    public void setVmNetwork(boolean vmNetwork) {
        this.vmNetwork = vmNetwork;
    }

}
