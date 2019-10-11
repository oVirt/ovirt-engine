package org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine;

import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabTableWidgetView;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.hostdev.VmHostDeviceListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineHostDevicePresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SubTabVirtualMachineHostDeviceView
        extends AbstractSubTabTableWidgetView<VM, HostDeviceView, VmListModel<Void>, VmHostDeviceListModel>
        implements SubTabVirtualMachineHostDevicePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabVirtualMachineHostDeviceView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabVirtualMachineHostDeviceView(
            SearchableDetailModelProvider<HostDeviceView, VmListModel<Void>, VmHostDeviceListModel> modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        super(new VmHostDeviceModelTable(modelProvider, eventBus, null, clientStorage));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getModelBoundTableWidget());
        bindSlot(AbstractSubTabPresenter.TYPE_SetActionPanel, getModelBoundTableWidget().getActionPanelContainer());
    }
}
