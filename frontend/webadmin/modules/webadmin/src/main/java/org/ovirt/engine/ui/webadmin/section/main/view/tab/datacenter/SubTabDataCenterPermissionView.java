package org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.webadmin.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.AbstractSubTabPermissionsView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;

import com.google.gwt.core.client.GWT;

public class SubTabDataCenterPermissionView extends AbstractSubTabPermissionsView<storage_pool, DataCenterListModel>
        implements SubTabDataCenterPermissionPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabDataCenterPermissionView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabDataCenterPermissionView(SearchableDetailModelProvider<permissions, DataCenterListModel, PermissionListModel> modelProvider) {
        super(modelProvider);
        initWidget(getTable());
    }

    @Override
    protected void initTable() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
        super.initTable();
    }

}
