package org.ovirt.engine.ui.uicommonweb.models.clusters;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

public class ClusterNetworkModel extends EntityModel<Network> {

    private final NetworkCluster originalNetworkCluster;

    private boolean attached = true;
    private Cluster cluster = null;

    public ClusterNetworkModel(Network network) {
        setEntity(network);
        if (network.getCluster() == null) {
            originalNetworkCluster = null;
            attached = false;
            // Init with default values
            getEntity().setCluster(new NetworkCluster(false));
        } else {
            originalNetworkCluster = (NetworkCluster) Cloner.clone(network.getCluster());
        }
    }

    public String getDisplayedName(){
        return getNetworkName();
    }

    public String getNetworkName() {
        return getEntity().getName();
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster){
        this.cluster = cluster;
    }

    public boolean isAttached() {
        return attached;
    }

    public boolean isDisplayNetwork() {
        return getEntity().getCluster().isDisplay();
    }

    public boolean isManagement() {
        return getEntity().getCluster().isManagement();
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

    public boolean isDefaultRouteNetwork() {
        return getEntity().getCluster().isDefaultRoute();
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
        getEntity().getCluster().setManagement(management);
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

    public void setDefaultRouteNetwork(boolean defaultRouteNetwork) {
        getEntity().getCluster().setDefaultRoute(defaultRouteNetwork);
    }

    public NetworkCluster getOriginalNetworkCluster() {
        return originalNetworkCluster;
    }

}
