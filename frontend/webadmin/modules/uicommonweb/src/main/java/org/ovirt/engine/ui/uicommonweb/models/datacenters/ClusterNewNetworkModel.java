package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public class ClusterNewNetworkModel extends NewNetworkModel{

    private final VDSGroup cluster;
    public ClusterNewNetworkModel(ListModel sourceListModel, VDSGroup cluster) {
        super(sourceListModel);
        this.cluster= cluster;
    }

    @Override
    protected NetworkClusterModel createNetworkClusterModel(VDSGroup cluster) {
        if ((cluster.getId().equals(this.cluster.getId()))) {
            NetworkClusterModel networkClusterModel = new NetworkClusterModel(cluster);
            networkClusterModel.setAttached(true);
            networkClusterModel.setRequired(!(Boolean) getExport().getEntity());
            networkClusterModel.setIsChangeable(false);

            return networkClusterModel;
         }else{
             return super.createNetworkClusterModel(cluster);
         }
    }

}
