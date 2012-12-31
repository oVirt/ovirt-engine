package org.ovirt.engine.ui.uicommonweb.models.clusters;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceListModel;

public class ClusterNetworkModel extends EntityModel {

    private boolean management;
    private boolean attached = true;
    private VDSGroup cluster = null;

    public ClusterNetworkModel(Network network) {
        setEntity(network);
        if (network.getCluster() == null){
            attached = false;
            // Init with default values
            getEntity().setCluster(new NetworkCluster());
        }
        if (HostInterfaceListModel.ENGINE_NETWORK_NAME.equals(network.getname())) {
            setManagement(true);
        }
    }

    @Override
    public Network getEntity() {
        return (Network) super.getEntity();
    }

    public String getDisplayedName(){
        return getNetworkName();
    }


    public String getNetworkName() {
        return getEntity().getname();
    }

    public VDSGroup getCluster() {
        return cluster;
    }

    public void setCluster(VDSGroup cluster){
        this.cluster = cluster;
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
        return getEntity().isVmNetwork();
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
        getEntity().setVmNetwork(vmNetwork);
    }

}
