package org.ovirt.engine.ui.common.widget.uicommon.vm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.DataBoundTabModelProvider;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.action.DropdownActionButton;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.common.widget.listgroup.PatternflyListView;
import org.ovirt.engine.ui.common.widget.listgroup.PatternflyListViewItem;
import org.ovirt.engine.ui.common.widget.listgroup.PatternflyListViewItemCreator;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
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

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public VmSnapshotListModelTable(DataBoundTabModelProvider<Snapshot, L> modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage, false);

        snapshotsTableContainer.add(actionPanel);
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
        initActionButtons();

        // Add event listeners
        addModelListeners();
    }

    private void initActionButtons() {
        addButtonToActionGroup(
        getTable().addActionButton(new UiCommandButtonDefinition<Snapshot>(getEventBus(), constants.createSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewCommand();
            }
        }));

        List<ActionButtonDefinition<Snapshot>> previewSubActions = new LinkedList<>();
        previewSubActions.add(new UiCommandButtonDefinition<Snapshot>(getEventBus(), constants.customPreviewSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getCustomPreviewCommand();
            }
        });
        addButtonToActionGroup(
        getTable().addActionButton(new UiCommandButtonDefinition<Snapshot>(
                getEventBus(), constants.previewSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getPreviewCommand();
            }
        }, new DropdownActionButton<>(previewSubActions, new DropdownActionButton.SelectedItemsProvider<Snapshot>() {
            @Override
            public List<Snapshot> getSelectedItems() {
                return getModel().getSelectedItems();
            }
        })));

        addButtonToActionGroup(
        getTable().addActionButton(new UiCommandButtonDefinition<Snapshot>(getEventBus(), constants.commitSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getCommitCommand();
            }
        }));
        addButtonToActionGroup(
        getTable().addActionButton(new UiCommandButtonDefinition<Snapshot>(getEventBus(), constants.undoSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getUndoCommand();
            }
        }));
        addButtonToActionGroup(
        getTable().addActionButton(new UiCommandButtonDefinition<Snapshot>(getEventBus(), constants.deleteSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        }));
        addButtonToActionGroup(
        getTable().addActionButton(new UiCommandButtonDefinition<Snapshot>(getEventBus(), constants.cloneSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getCloneVmCommand();
            }
        }));
        addButtonToActionGroup(
        getTable().addActionButton(new UiCommandButtonDefinition<Snapshot>(getEventBus(), constants.makeTemplateFromSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getCloneTemplateCommand();
            }
        }));
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
