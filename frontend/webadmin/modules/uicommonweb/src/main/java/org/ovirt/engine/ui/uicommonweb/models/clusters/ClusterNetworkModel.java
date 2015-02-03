package org.ovirt.engine.ui.uicommonweb.models.clusters;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceListModel;

public class ClusterNetworkModel extends EntityModel<Network> {

    private final NetworkCluster originalNetworkCluster;

    private boolean management;
    private boolean attached = true;
    private VDSGroup cluster = null;

    public ClusterNetworkModel(Network network) {
        setEntity(network);
        if (network.getCluster() == null) {
            originalNetworkCluster = null;
            attached = false;
            // Init with default values
            getEntity().setCluster(new NetworkCluster(!network.isExternal()));
        } else {
            originalNetworkCluster = (NetworkCluster) Cloner.clone(network.getCluster());
        }
        if (HostInterfaceListModel.ENGINE_NETWORK_NAME.equals(network.getName())) {
            setManagement(true);
        }
    }

    public String getDisplayedName(){
        return getNetworkName();
    }

    public String getNetworkName() {
        return getEntity().getName();
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
        return getEntity().getCluster().isDisplay();
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

    public boolean isMigrationNetwork() {
        return getEntity().getCluster().isMigration();
    }

    public boolean isGlusterNetwork() {
        return getEntity().getCluster().isGluster();
    }

    public boolean isExternal() {
        return getEntity().isExternal();
    }

    public void setAttached(boolean attached) {
        this.attached = attached;
    }

    public void setDisplayNetwork(boolean displayNetwork) {
        getEntity().getCluster().setDisplay(displayNetwork);
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

    public void setMigrationNetwork(boolean migrationNetwork) {
        getEntity().getCluster().setMigration(migrationNetwork);
    }

    public void setGlusterNetwork(boolean glusterNetwork) {
        getEntity().getCluster().setGluster(glusterNetwork);
    }

    public NetworkCluster getOriginalNetworkCluster() {
        return originalNetworkCluster;
    }

}
