package org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractDetailTabListView;
import org.ovirt.engine.ui.common.widget.action.SnapshotActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.widget.listgroup.PatternflyListView;
import org.ovirt.engine.ui.common.widget.listgroup.PatternflyListViewItem;
import org.ovirt.engine.ui.common.widget.listgroup.PatternflyListViewItemCreator;
import org.ovirt.engine.ui.common.widget.uicommon.vm.VmSnapshotListViewItem;
import org.ovirt.engine.ui.uicommonweb.models.vms.SnapshotModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSnapshotListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineSnapshotPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

public class SubTabVirtualMachineSnapshotView extends AbstractDetailTabListView<VM, VmListModel<Void>,
    VmSnapshotListModel> implements SubTabVirtualMachineSnapshotPresenter.ViewDef,
        PatternflyListViewItemCreator<Snapshot> {

    interface ViewIdHandler extends ElementIdHandler<SubTabVirtualMachineSnapshotView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private PatternflyListView<VM, Snapshot, VmSnapshotListModel> snapshotListView;

    @Inject
    public SubTabVirtualMachineSnapshotView(SearchableDetailModelProvider<Snapshot, VmListModel<Void>,
                VmSnapshotListModel> modelProvider,
            EventBus eventBus, SnapshotActionPanelPresenterWidget actionPanel,
            ClientStorage clientStorage) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        snapshotListView = new PatternflyListView<>();

        snapshotListView.setCreator(this);
        snapshotListView.setModel(modelProvider.getModel());
        getContentPanel().add(snapshotListView);
        initWidget(getContentPanel());
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == AbstractSubTabPresenter.TYPE_SetActionPanel) {
            getContainer().insert(content, 0);
        } else {
            super.setInSlot(slot, content);
        }
    }

    @Override
    public void setMainSelectedItem(VM selectedItem) {
        // Not interested in current selected VM.
    }

    @Override
    public PatternflyListViewItem<Snapshot> createListViewItem(Snapshot selectedItem) {
        Map<Guid, SnapshotModel> snapshotsMap = getDetailModel().getSnapshotsMap();
        SnapshotModel snapshotModel = snapshotsMap.get(selectedItem.getId());
        VmSnapshotListViewItem newItem = new VmSnapshotListViewItem(selectedItem.getDescription(), selectedItem,
                getDetailModel(), snapshotModel);
        snapshotModel.updateVmConfiguration(returnValue -> newItem.updateValues(snapshotModel));
        return newItem;
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }
}
