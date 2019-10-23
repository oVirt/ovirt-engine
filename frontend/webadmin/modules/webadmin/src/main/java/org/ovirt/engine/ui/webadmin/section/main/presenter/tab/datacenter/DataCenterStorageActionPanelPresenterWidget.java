package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterStorageListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class DataCenterStorageActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<StoragePool, StorageDomain, DataCenterListModel, DataCenterStorageListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public DataCenterStorageActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<StoragePool, StorageDomain> view,
            SearchableDetailModelProvider<StorageDomain, DataCenterListModel, DataCenterStorageListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<StoragePool, StorageDomain>(constants.attachDataStorage()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getAttachStorageCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<StoragePool, StorageDomain>(constants.attachIsoStorage()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getAttachISOCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<StoragePool, StorageDomain>(constants.attachExportStorage()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getAttachBackupCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<StoragePool, StorageDomain>(constants.detachStorage()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getDetachCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<StoragePool, StorageDomain>(constants.activateStorage()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getActivateCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<StoragePool, StorageDomain>(constants.maintenanceHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getMaintenanceCommand();
            }
        });
    }

}
