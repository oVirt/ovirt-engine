package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.AbstrctSubTabPermissionsView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;

public class SubTabClusterPermissionView extends AbstrctSubTabPermissionsView<VDSGroup, ClusterListModel>
        implements SubTabClusterPermissionPresenter.ViewDef {

    @Inject
    public SubTabClusterPermissionView(SearchableDetailModelProvider<permissions, ClusterListModel, PermissionListModel> modelProvider) {
        super(modelProvider);
        initWidget(getTable());
    }

}
