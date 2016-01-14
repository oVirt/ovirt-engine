package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public class ClusterNewNetworkModel extends NewNetworkModel{

    private final Cluster cluster;
    public ClusterNewNetworkModel(ListModel sourceListModel, Cluster cluster) {
        super(sourceListModel);
        this.cluster= cluster;
    }

    @Override
    protected NetworkClusterModel createNetworkClusterModel(Cluster cluster) {
        if (cluster.getId().equals(this.cluster.getId())) {
            NetworkClusterModel networkClusterModel = new NetworkClusterModel(cluster);
            networkClusterModel.setAttached(true);
            networkClusterModel.setRequired(!getExport().getEntity());
            networkClusterModel.setIsChangeable(false);

            return networkClusterModel;
         }else{
             return super.createNetworkClusterModel(cluster);
         }
    }

}
