package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.VmBackupModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class StorageVmBackupActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<StorageDomain, VM, StorageListModel, VmBackupModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public StorageVmBackupActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<StorageDomain, VM> view,
            SearchableDetailModelProvider<VM, StorageListModel, VmBackupModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<StorageDomain, VM>(constants.restoreVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRestoreCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<StorageDomain, VM>(constants.removeVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
