package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.List;

import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public class ClusterNetworkManageModel extends ListModel{

    public boolean isMultiDisplay(){
        return false;
    }

    @Override
    public List<ClusterNetworkModel> getItems() {
        return (List<ClusterNetworkModel>) super.getItems();
    }

    public ClusterNetworkModel getDisplayNetwork(){
        if (!isMultiDisplay()){
            for (ClusterNetworkModel clusterNetworkManageModel : getItems()) {
                if (clusterNetworkManageModel.isDisplayNetwork()) {
                   return clusterNetworkManageModel;
                }
            }
        }
        return null;
    }

    public void setDisplayNetwork(ClusterNetworkModel model, boolean value){
        if (!isMultiDisplay()){
            // Reset the old display
            if (getDisplayNetwork()!= null){
                getDisplayNetwork().setDisplayNetwork(!value);
            }
        }
        model.setDisplayNetwork(value);
    }

}
