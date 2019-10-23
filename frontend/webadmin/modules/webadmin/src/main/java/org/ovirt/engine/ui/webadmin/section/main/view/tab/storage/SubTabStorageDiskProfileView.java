package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabTableWidgetView;
import org.ovirt.engine.ui.common.widget.action.PermissionActionPanelPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.DiskProfileListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.DiskProfileActionPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageDiskProfilePresenter;
import org.ovirt.engine.ui.webadmin.uicommon.model.DiskProfilePermissionModelProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SubTabStorageDiskProfileView extends AbstractSubTabTableWidgetView<StorageDomain, DiskProfile, StorageListModel, DiskProfileListModel>
        implements SubTabStorageDiskProfilePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabStorageDiskProfileView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabStorageDiskProfileView(SearchableDetailModelProvider<DiskProfile, StorageListModel, DiskProfileListModel> modelProvider,
            DiskProfilePermissionModelProvider permissionModelProvider,
            DiskProfileActionPanelPresenterWidget actionPanel,
            PermissionActionPanelPresenterWidget<DiskProfile, DiskProfileListModel, PermissionListModel<DiskProfile>> permissionActionPanel,
            EventBus eventBus,
            ClientStorage clientStorage) {
        super(new DiskProfilesListModelTable(modelProvider,
                permissionModelProvider,
                eventBus,
                permissionActionPanel,
                actionPanel,
                clientStorage));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getModelBoundTableWidget());
    }

    @Override
    public void addModelListeners() {
        getModelBoundTableWidget().addModelListeners();
    }
}
