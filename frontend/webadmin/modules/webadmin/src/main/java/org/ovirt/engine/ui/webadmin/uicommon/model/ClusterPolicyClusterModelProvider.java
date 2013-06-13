package org.ovirt.engine.ui.webadmin.uicommon.model;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.ClusterPolicyClusterListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;

import com.google.inject.Inject;

public class ClusterPolicyClusterModelProvider extends SearchableTabModelProvider<VDSGroup, ClusterPolicyClusterListModel> {

    @Inject
    public ClusterPolicyClusterModelProvider(ClientGinjector ginjector) {
        super(ginjector);
    }

    @Override
    public ClusterPolicyClusterListModel getModel() {
        return (ClusterPolicyClusterListModel) getCommonModel().getClusterPolicyListModel().getDetailModels().get(0);
    }

}
