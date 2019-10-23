package org.ovirt.engine.ui.webadmin.section.main.view.tab.pool;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.action.PermissionActionPanelPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool.SubTabPoolPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.AbstractSubTabPermissionsView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;

public class SubTabPoolPermissionView extends AbstractSubTabPermissionsView<VmPool, PoolListModel>
        implements SubTabPoolPermissionPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabPoolPermissionView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabPoolPermissionView(SearchableDetailModelProvider<Permission, PoolListModel,
            PermissionListModel<VmPool>> modelProvider, EventBus eventBus,
            PermissionActionPanelPresenterWidget<VmPool, PoolListModel, PermissionListModel<VmPool>> actionPanel,
            ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage, actionPanel);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
