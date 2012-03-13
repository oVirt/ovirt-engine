package org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabTableWidgetView;
import org.ovirt.engine.ui.common.widget.uicommon.vm.VmDiskListModelTable;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineVirtualDiskPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SubTabVirtualMachineVirtualDiskView extends AbstractSubTabTableWidgetView<VM, DiskImage, VmListModel, VmDiskListModel> implements SubTabVirtualMachineVirtualDiskPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabVirtualMachineVirtualDiskView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabVirtualMachineVirtualDiskView(
            SearchableDetailModelProvider<DiskImage, VmListModel, VmDiskListModel> modelProvider,
            EventBus eventBus, ClientStorage clientStorage, ApplicationResources resources,
            ApplicationConstants constants) {
        super(new VmDiskListModelTable(modelProvider, eventBus, clientStorage, resources, constants, true));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getModelBoundTableWidget());
    }

}
