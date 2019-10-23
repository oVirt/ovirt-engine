package org.ovirt.engine.ui.webadmin.section.main.view.tab.network;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.action.PermissionActionPanelPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.AbstractSubTabPermissionsView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SubTabNetworkPermissionView extends AbstractSubTabPermissionsView<NetworkView, NetworkListModel>
        implements SubTabNetworkPermissionPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabNetworkPermissionView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabNetworkPermissionView(SearchableDetailModelProvider<Permission, NetworkListModel,
            PermissionListModel<NetworkView>> modelProvider, EventBus eventBus,
            PermissionActionPanelPresenterWidget<NetworkView, NetworkListModel, PermissionListModel<NetworkView>> actionPanel,
            ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage, actionPanel);
        getTable().enableColumnResizing();
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}

