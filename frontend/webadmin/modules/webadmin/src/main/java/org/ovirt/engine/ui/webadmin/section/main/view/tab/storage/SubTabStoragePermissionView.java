package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.action.PermissionActionPanelPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStoragePermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.AbstractSubTabPermissionsView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SubTabStoragePermissionView extends AbstractSubTabPermissionsView<StorageDomain, StorageListModel>
        implements SubTabStoragePermissionPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabStoragePermissionView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabStoragePermissionView(SearchableDetailModelProvider<Permission, StorageListModel,
            PermissionListModel<StorageDomain>> modelProvider, EventBus eventBus,
            PermissionActionPanelPresenterWidget<StorageDomain, StorageListModel, PermissionListModel<StorageDomain>> actionPanel,
            ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage, actionPanel);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
