package org.ovirt.engine.ui.webadmin.section.main.view.tab.pool;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel;
import org.ovirt.engine.ui.webadmin.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool.SubTabPoolPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.AbstractSubTabPermissionsView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;

import com.google.gwt.core.client.GWT;

public class SubTabPoolPermissionView extends AbstractSubTabPermissionsView<vm_pools, PoolListModel>
        implements SubTabPoolPermissionPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabPoolPermissionView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabPoolPermissionView(SearchableDetailModelProvider<permissions, PoolListModel, PermissionListModel> modelProvider) {
        super(modelProvider);
        initWidget(getTable());
    }

    @Override
    protected void initTable() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
        super.initTable();
    }

}
