package org.ovirt.engine.ui.common.widget.uicommon.vm;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.CommandVersionsInfo;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.DataBoundTabModelProvider;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.action.DropdownActionButton;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.common.widget.uicommon.snapshot.SnapshotsViewColumns;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.SnapshotModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSnapshotListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class VmSnapshotListModelTable<L extends VmSnapshotListModel> extends AbstractModelBoundTableWidget<Snapshot, L> {

    interface WidgetUiBinder extends UiBinder<Widget, VmSnapshotListModelTable<?>> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    LayoutPanel snapshotsTableContainer;

    @UiField
    SimplePanel snapshotInfoContainer;

    private final CommonApplicationMessages messages;
    private final CommonApplicationConstants constants;

    private final IEventListener entityChangedEvent = new IEventListener() {
        @Override
        public void eventRaised(Event ev, Object sender, EventArgs args) {
            updateMemoryColumnVisibility();
        }
    };

    private final IEventListener selectedItemChangedEvent = new IEventListener() {
        @Override
        public void eventRaised(Event ev, Object sender, EventArgs args) {
            updateSnapshotInfo();
        }
    };

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
        vmSnapshotInfoPanel = new VmSnapshotInfoPanel(constants, messages, templates);
        snapshotInfoContainer.add(vmSnapshotInfoPanel);
    }

    @Override
    protected Widget getWrappedWidget() {
        return WidgetUiBinder.uiBinder.createAndBindUi(this);
    }

    @Override
    public void initTable(final CommonApplicationConstants constants) {
        getTable().enableColumnResizing();
        getTable().setMultiSelectionDisabled(true);

        initActionButtons(constants);

        // Add event listeners
        addModelListeners();
    }

    @Override
    public void addModelListeners() {
        if (!getModel().getEntityChangedEvent().getListeners().contains(entityChangedEvent)) {
            getModel().getEntityChangedEvent().addListener(entityChangedEvent);
        }
        if (!getModel().getSelectedItemChangedEvent().getListeners().contains(selectedItemChangedEvent)) {
            getModel().getSelectedItemChangedEvent().addListener(selectedItemChangedEvent);
        }
    }

    private void updateSnapshotInfo() {
        final Snapshot snapshot = (Snapshot) getModel().getSelectedItem();
        if (snapshot == null) {
            return;
        }

        HashMap<Guid, SnapshotModel> snapshotsMap = getModel().getSnapshotsMap();
        SnapshotModel snapshotModel = snapshotsMap.get(snapshot.getId());
        vmSnapshotInfoPanel.updatePanel(snapshotModel != null ? snapshotModel : new SnapshotModel());

        if (!getTable().getSelectionModel().isSelected(snapshot)) {
            // first let list of items get updated, only then select item
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    getTable().getSelectionModel().setSelected(snapshot, true);
                }
            });
        }
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
                getModel().isMemorySnapshotSupported();

        getTable().ensureColumnPresent(SnapshotsViewColumns.memoryColumn,
                constants.memorySnapshot(), memorySnapshotSupported, "55px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(SnapshotsViewColumns.descriptionColumn,
                constants.descriptionSnapshot(), true, "300px"); //$NON-NLS-1$
    }

    private void initActionButtons(final CommonApplicationConstants constants) {
        getTable().addActionButton(new UiCommandButtonDefinition<Snapshot>(getEventBus(), constants.createSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewCommand();
            }
        });

        List<ActionButtonDefinition<Snapshot>> previewSubActions = new LinkedList<ActionButtonDefinition<Snapshot>>();
        previewSubActions.add(new UiCommandButtonDefinition<Snapshot>(getEventBus(), constants.customPreviewSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getCustomPreviewCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<Snapshot>(
                getEventBus(), constants.previewSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getPreviewCommand();
            }
        }, new DropdownActionButton<Snapshot>(previewSubActions, getModel().getSelectedItems()));


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
}
