package org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabTreeWidgetView;
import org.ovirt.engine.ui.common.widget.action.SubTabTreeActionPanel;
import org.ovirt.engine.ui.common.widget.uicommon.vm.VmSnapshotListModelTree;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSnapshotListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineSnapshotPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;


public class SubTabVirtualMachineSnapshotView extends AbstractSubTabTreeWidgetView<VM, Snapshot, VmListModel, VmSnapshotListModel> implements SubTabVirtualMachineSnapshotPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabVirtualMachineSnapshotView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabVirtualMachineSnapshotView(SearchableDetailModelProvider<Snapshot, VmListModel, VmSnapshotListModel> modelProvider,
            EventBus eventBus,
            CommonApplicationResources resources,
            CommonApplicationConstants constants) {
        super(new VmSnapshotListModelTree(modelProvider, eventBus, resources, constants), eventBus);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        getModelBoundTreeWidget().initTree(actionPanel, table);
    }

    @Override
    protected SubTabTreeActionPanel createActionPanel(SearchableDetailModelProvider modelProvider) {
        return new SubTabTreeActionPanel<Snapshot>(modelProvider, getEventBus());
    }

}
