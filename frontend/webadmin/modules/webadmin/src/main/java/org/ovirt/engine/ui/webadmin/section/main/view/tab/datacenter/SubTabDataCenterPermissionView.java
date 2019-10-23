package org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.action.PermissionActionPanelPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.AbstractSubTabPermissionsView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;

public class SubTabDataCenterPermissionView extends AbstractSubTabPermissionsView<StoragePool, DataCenterListModel>
        implements SubTabDataCenterPermissionPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabDataCenterPermissionView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabDataCenterPermissionView(SearchableDetailModelProvider<Permission, DataCenterListModel,
            PermissionListModel<StoragePool>> modelProvider,
            EventBus eventBus,
            PermissionActionPanelPresenterWidget<StoragePool, DataCenterListModel, PermissionListModel<StoragePool>> actionPanel,
            ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage, actionPanel);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
