package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageRegisterDiskImageListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class StorageRegisterDiskImageActionPanelPresenterWidget
    extends DetailActionPanelPresenterWidget<StorageDomain, Disk, StorageListModel, StorageRegisterDiskImageListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public StorageRegisterDiskImageActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<StorageDomain, Disk> view,
            SearchableDetailModelProvider<Disk, StorageListModel, StorageRegisterDiskImageListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<StorageDomain, Disk>(constants.registerDiskImage()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRegisterCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<StorageDomain, Disk>(constants.removeDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
