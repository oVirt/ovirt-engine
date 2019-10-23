package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class StorageDataCenterActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<StorageDomain, StorageDomain, StorageListModel, StorageDataCenterListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public StorageDataCenterActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<StorageDomain, StorageDomain> view,
            SearchableDetailModelProvider<StorageDomain, StorageListModel, StorageDataCenterListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<StorageDomain, StorageDomain>(constants.attachStorageDc()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getAttachCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<StorageDomain, StorageDomain>(constants.detachStorageDc()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getDetachCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<StorageDomain, StorageDomain>(constants.activateStorageDc()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getActivateCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<StorageDomain, StorageDomain>(constants.maintenanceStorageDc()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getMaintenanceCommand();
            }
        });
    }

}
