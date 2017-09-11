package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.gluster.HostGlusterStorageDevicesListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class HostGlusterActionPanelPresenterWidget extends DetailActionPanelPresenterWidget<StorageDevice, HostListModel<Void>, HostGlusterStorageDevicesListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public HostGlusterActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<StorageDevice> view,
            SearchableDetailModelProvider<StorageDevice, HostListModel<Void>, HostGlusterStorageDevicesListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<StorageDevice>(constants.createBrick()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getCreateBrickCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<StorageDevice>(constants.syncStorageDevices()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getSyncStorageDevicesCommand();
            }
        });
    }

}
