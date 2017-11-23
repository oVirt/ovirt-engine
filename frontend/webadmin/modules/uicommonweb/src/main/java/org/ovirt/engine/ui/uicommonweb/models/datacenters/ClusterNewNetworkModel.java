package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

public class ClusterNewNetworkModel extends NewNetworkModel{

    private final Cluster cluster;
    public ClusterNewNetworkModel(SearchableListModel<?, ? extends Network> sourceListModel, Cluster cluster) {
        super(sourceListModel);
        this.cluster= cluster;
    }

    @Override
    protected NetworkClusterModel createNetworkClusterModel(Cluster cluster) {
        if (cluster.getId().equals(this.cluster.getId())) {
            NetworkClusterModel networkClusterModel = new NetworkClusterModel(cluster);
            networkClusterModel.setAttached(true);
            networkClusterModel.setRequired(!getExternal().getEntity());
            networkClusterModel.setIsChangeable(false);

            return networkClusterModel;
         }else{
             return super.createNetworkClusterModel(cluster);
         }
    }

}
