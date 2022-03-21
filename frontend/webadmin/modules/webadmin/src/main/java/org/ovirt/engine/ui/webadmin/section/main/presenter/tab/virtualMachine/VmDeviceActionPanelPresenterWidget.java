package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDeviceFeEntity;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDevicesListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;

import com.google.web.bindery.event.shared.EventBus;

public class VmDeviceActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<VM, VmDeviceFeEntity, VmListModel<Void>, VmDevicesListModel<VM>> {

    @Inject
    public VmDeviceActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<VM, VmDeviceFeEntity> view,
            SearchableDetailModelProvider<VmDeviceFeEntity, VmListModel<Void>, VmDevicesListModel<VM>> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        // no GWT action buttons
    }
}
