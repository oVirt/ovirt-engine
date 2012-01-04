package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.webadmin.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.AbstractSubTabPermissionsView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;

import com.google.gwt.core.client.GWT;

public class SubTabClusterPermissionView extends AbstractSubTabPermissionsView<VDSGroup, ClusterListModel>
        implements SubTabClusterPermissionPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabClusterPermissionView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabClusterPermissionView(SearchableDetailModelProvider<permissions, ClusterListModel, PermissionListModel> modelProvider) {
        super(modelProvider);
        initWidget(getTable());
    }

    @Override
    protected void initTable() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
        super.initTable();
    }

}
