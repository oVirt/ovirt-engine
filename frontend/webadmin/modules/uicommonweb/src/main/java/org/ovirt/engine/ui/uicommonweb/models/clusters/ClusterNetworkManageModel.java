package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.List;

import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public class ClusterNetworkManageModel extends ListModel{

    public boolean isMultiCluster(){
        return false;
    }

    @Override
    public List<ClusterNetworkModel> getItems() {
        return (List<ClusterNetworkModel>) super.getItems();
    }

    private ClusterNetworkModel getDisplayNetwork() {
        if (!isMultiCluster()){
            for (ClusterNetworkModel clusterNetworkManageModel : getItems()) {
                if (clusterNetworkManageModel.isDisplayNetwork()) {
                   return clusterNetworkManageModel;
                }
            }
        }
        return null;
    }

    public void setDisplayNetwork(ClusterNetworkModel model, boolean value){
        if (!isMultiCluster()){
            // Reset the old display
            if (getDisplayNetwork()!= null){
                getDisplayNetwork().setDisplayNetwork(!value);
            }
        }
        model.setDisplayNetwork(value);
    }

    private ClusterNetworkModel getMigrationNetwork() {
        if (!isMultiCluster()) {
            for (ClusterNetworkModel clusterNetworkManageModel : getItems()) {
                if (clusterNetworkManageModel.isMigrationNetwork()) {
                    return clusterNetworkManageModel;
                }
            }
        }
        return null;
    }

    public void setMigrationNetwork(ClusterNetworkModel model, boolean value) {
        if (!isMultiCluster()) {
            // Reset the old migration
            if (getMigrationNetwork() != null) {
                getMigrationNetwork().setMigrationNetwork(!value);
            }
        }
        model.setMigrationNetwork(value);
    }

}
