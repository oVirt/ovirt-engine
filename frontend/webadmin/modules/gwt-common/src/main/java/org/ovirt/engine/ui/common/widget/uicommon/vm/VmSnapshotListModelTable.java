package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.CommandVersionsInfo;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.DataBoundTabModelProvider;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.common.widget.table.OrderedMultiSelectionModel;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.common.widget.uicommon.snapshot.SnapshotsViewColumns;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSnapshotListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.DefaultSelectionEventManager.EventTranslator;
import com.google.gwt.view.client.DefaultSelectionEventManager.SelectAction;

public class VmSnapshotListModelTable<L extends VmSnapshotListModel> extends AbstractModelBoundTableWidget<Snapshot, L> {

    interface WidgetUiBinder extends UiBinder<Widget, VmSnapshotListModelTable<?>> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    SimplePanel snapshotsTableContainer;

    @UiField
    SimplePanel snapshotInfoContainer;

    private final CommonApplicationMessages messages;
    private final CommonApplicationConstants constants;

    VmSnapshotInfoPanel vmSnapshotInfoPanel;

    public VmSnapshotListModelTable(DataBoundTabModelProvider<Snapshot, L> modelProvider,
            EventBus eventBus, ClientStorage clientStorage,
            CommonApplicationConstants constants,
            CommonApplicationMessages messages,
            CommonApplicationTemplates templates) {
        super(modelProvider, eventBus, clientStorage, false);

        this.constants = constants;
        this.messages = messages;

        // Create Snapshots table
        SimpleActionTable<Snapshot> table = getTable();
        snapshotsTableContainer.add(table);

        // Create Snapshot information tab panel
        vmSnapshotInfoPanel = new VmSnapshotInfoPanel(getModel(), constants, messages, templates);
        snapshotInfoContainer.add(vmSnapshotInfoPanel);
    }

    @Override
    protected Widget getWrappedWidget() {
        return WidgetUiBinder.uiBinder.createAndBindUi(this);
    }

    @Override
    public void initTable(final CommonApplicationConstants constants) {
        getTable().enableColumnResizing();

        initActionButtons(constants);
        disableActiveSnapshotRow();
        getModel().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                updateMemoryColumnVisibility();
            }
        });

        // Add selection listener
        getModel().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                Snapshot snapshot = (Snapshot) getModel().getSelectedItem();
                if (snapshot != null && !getTable().getSelectionModel().isSelected(snapshot)) {
                    getTable().getSelectionModel().setSelected(snapshot, true);
                }
                vmSnapshotInfoPanel.updatePanel(snapshot);
            }
        });
    }

    private void updateMemoryColumnVisibility() {
        VM vm = (VM) getModel().getEntity();
        if (vm == null) {
            return;
        }

        getTable().ensureColumnPresent(SnapshotsViewColumns.dateColumn,
                constants.dateSnapshot(), true, "150px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(SnapshotsViewColumns.statusColumn,
                constants.statusSnapshot(), true, "75px"); //$NON-NLS-1$

        boolean memorySnapshotSupported =
                (Boolean) AsyncDataProvider.getConfigValuePreConverted(
                        ConfigurationValues.MemorySnapshotSupported,
                        vm.getVdsGroupCompatibilityVersion().toString());

        getTable().ensureColumnPresent(SnapshotsViewColumns.memoryColumn,
                constants.memorySnapshot(), memorySnapshotSupported, "55px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(SnapshotsViewColumns.descriptionColumn,
                constants.descriptionSnapshot(), true, "185px"); //$NON-NLS-1$
    }

    private void initActionButtons(final CommonApplicationConstants constants) {
        getTable().addActionButton(new UiCommandButtonDefinition<Snapshot>(getEventBus(), constants.createSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<Snapshot>(getEventBus(), constants.previewSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getPreviewCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<Snapshot>(getEventBus(), constants.commitSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getCommitCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<Snapshot>(getEventBus(), constants.undoSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getUndoCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<Snapshot>(getEventBus(), constants.deleteSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<Snapshot>(getEventBus(), constants.cloneSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getCloneVmCommand();
            }

            @Override
            public String getButtonToolTip() {
                if (!getModel().getIsCloneVmSupported() && getModel().getEntity() != null) {
                    CommandVersionsInfo commandVersionsInfo =
                            AsyncDataProvider.getCommandVersionsInfo(VdcActionType.AddVmFromSnapshot);
                    String minimalClusterVersion = commandVersionsInfo != null ?
                            commandVersionsInfo.getClusterVersion().toString(2) : ""; //$NON-NLS-1$
                    return messages.cloneVmNotSupported(minimalClusterVersion);
                } else {
                    return this.getTitle();
                }
            }
        });
    }

    private void disableActiveSnapshotRow() {
        // Create a selection event manager (to disable 'current' snapshot selection)
        DefaultSelectionEventManager<Snapshot> selectionEventManager =
                DefaultSelectionEventManager.createCustomManager(new EventTranslator<Snapshot>() {
                    @Override
                    public boolean clearCurrentSelection(CellPreviewEvent<Snapshot> event) {
                        return true;
                    }

                    @Override
                    public SelectAction translateSelectionEvent(CellPreviewEvent<Snapshot> event) {
                        if (event.getValue().getType() == SnapshotType.ACTIVE) {
                            return SelectAction.IGNORE;
                        }

                        return SelectAction.DEFAULT;
                    }
                });

        // Set selection mode, disable multiselection and first row ('current' snapshot)
        OrderedMultiSelectionModel<Snapshot> selectionModel = getTable().getSelectionModel();
        selectionModel.setDisabledRows(0);
        getTable().setTableSelectionModel(selectionModel, selectionEventManager);
        getTable().setMultiSelectionDisabled(true);
    }

}
