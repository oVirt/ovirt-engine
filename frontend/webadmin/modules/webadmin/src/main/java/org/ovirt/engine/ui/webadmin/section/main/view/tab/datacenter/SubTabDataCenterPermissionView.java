package org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.AbstrctSubTabPermissionsView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;

public class SubTabDataCenterPermissionView extends AbstrctSubTabPermissionsView<storage_pool, DataCenterListModel>
        implements SubTabDataCenterPermissionPresenter.ViewDef {

    @Inject
    public SubTabDataCenterPermissionView(SearchableDetailModelProvider<permissions, DataCenterListModel, PermissionListModel> modelProvider) {
        super(modelProvider);
        initWidget(getTable());
    }

}
