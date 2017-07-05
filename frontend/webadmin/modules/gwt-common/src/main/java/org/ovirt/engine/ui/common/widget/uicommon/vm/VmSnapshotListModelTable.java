package org.ovirt.engine.ui.common.widget.uicommon.vm;

import java.util.HashMap;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.DataBoundTabModelProvider;
import org.ovirt.engine.ui.common.widget.listgroup.PatternflyListView;
import org.ovirt.engine.ui.common.widget.listgroup.PatternflyListViewItem;
import org.ovirt.engine.ui.common.widget.listgroup.PatternflyListViewItemCreator;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.SnapshotModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSnapshotListModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class VmSnapshotListModelTable<L extends VmSnapshotListModel> extends AbstractModelBoundTableWidget<Snapshot, L>
    implements PatternflyListViewItemCreator<Snapshot> {

    interface WidgetUiBinder extends UiBinder<Widget, VmSnapshotListModelTable<?>> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    FlowPanel snapshotsTableContainer;

    @UiField
    PatternflyListView<VM, Snapshot, VmSnapshotListModel> snapshotListView;

    public VmSnapshotListModelTable(DataBoundTabModelProvider<Snapshot, L> modelProvider,
            EventBus eventBus, ActionPanelPresenterWidget<Snapshot, L> actionPanel, ClientStorage clientStorage) {
        super(modelProvider, eventBus, null, clientStorage, false);

        snapshotsTableContainer.add(actionPanel.getView());
        snapshotListView.setModel(modelProvider.getModel());
        snapshotListView.setSelectionModel(getTable().getSelectionModel());
        snapshotListView.setCreator(this);
    }

    @Override
    protected Widget getWrappedWidget() {
        return WidgetUiBinder.uiBinder.createAndBindUi(this);
    }

    @Override
    public void initTable() {
        // Add event listeners
        addModelListeners();
    }

    @Override
    public PatternflyListViewItem<Snapshot> createListViewItem(Snapshot selectedItem) {
        HashMap<Guid, SnapshotModel> snapshotsMap = getModel().getSnapshotsMap();
        SnapshotModel snapshotModel = snapshotsMap.get(selectedItem.getId());
        VmSnapshotListViewItem newItem = new VmSnapshotListViewItem(selectedItem.getDescription(), selectedItem,
                getModel(), snapshotModel);
        snapshotModel.updateVmConfiguration(returnValue -> newItem.updateValues(snapshotModel));
        return newItem;
    }
}
