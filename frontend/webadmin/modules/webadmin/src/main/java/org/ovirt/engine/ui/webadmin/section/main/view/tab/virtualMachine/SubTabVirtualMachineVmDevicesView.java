package org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabTableWidgetView;
import org.ovirt.engine.ui.common.widget.HasCellClickHandlers;
import org.ovirt.engine.ui.common.widget.uicommon.vm.VmDevicesListModelTable;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDeviceFeEntity;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDevicesListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineVmDevicePresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SubTabVirtualMachineVmDevicesView extends AbstractSubTabTableWidgetView<VM, VmDeviceFeEntity, VmListModel<Void>, VmDevicesListModel<VM>>
        implements SubTabVirtualMachineVmDevicePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabVirtualMachineVmDevicesView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabVirtualMachineVmDevicesView(
            SearchableDetailModelProvider<VmDeviceFeEntity, VmListModel<Void>, VmDevicesListModel<VM>> modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        super(new VmDevicesListModelTable(modelProvider, eventBus, clientStorage));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getModelBoundTableWidget());
    }

    @Override
    public HasCellClickHandlers<VmDeviceFeEntity> getHotUnplugColumn() {
        return ((VmDevicesListModelTable) getModelBoundTableWidget()).getHotUnplugColumn();
    }
}
