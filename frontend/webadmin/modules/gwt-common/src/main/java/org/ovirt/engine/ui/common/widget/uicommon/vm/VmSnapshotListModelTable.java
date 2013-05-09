package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.queries.CommandVersionsInfo;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.DataBoundTabModelProvider;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.common.widget.renderer.FullDateTimeRenderer;
import org.ovirt.engine.ui.common.widget.table.OrderedMultiSelectionModel;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.SafeHtmlColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSnapshotListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
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
    private final CommonApplicationTemplates templates;

    VmSnapshotInfoPanel vmSnapshotInfoPanel;

    public VmSnapshotListModelTable(DataBoundTabModelProvider<Snapshot, L> modelProvider,
            EventBus eventBus, ClientStorage clientStorage,
            CommonApplicationConstants constants,
            CommonApplicationMessages messages,
            CommonApplicationTemplates templates) {
        super(modelProvider, eventBus, clientStorage, false);

        this.messages = messages;
        this.templates = templates;

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

        TextColumnWithTooltip<Snapshot> dateColumn = new TextColumnWithTooltip<Snapshot>() {
            @Override
            public String getValue(Snapshot snapshot) {
                if (snapshot.getType() == SnapshotType.ACTIVE) {
                    return constants.currentSnapshotLabel();
                }
                return FullDateTimeRenderer.getLocalizedDateTimeFormat().format(snapshot.getCreationDate());
            }
        };
        getTable().addColumn(dateColumn, constants.dateSnapshot(), "185px"); //$NON-NLS-1$

        TextColumnWithTooltip<Snapshot> statusColumn = new EnumColumn<Snapshot, SnapshotStatus>() {
            @Override
            protected SnapshotStatus getRawValue(Snapshot snapshot) {
                return snapshot.getStatus();
            }
        };
        getTable().addColumn(statusColumn, constants.statusSnapshot(), "95px"); //$NON-NLS-1$

        SafeHtmlColumn<Snapshot> descriptionColumn = new SafeHtmlColumn<Snapshot>() {
            @Override
            public final SafeHtml getValue(Snapshot snapshot) {
                // Get raw description string (ignore < and > characters).
                // Customize description style as needed.
                SafeHtml description = SafeHtmlUtils.fromString(snapshot.getDescription());
                String descriptionStr = description.asString();

                if (snapshot.getStatus() == SnapshotStatus.IN_PREVIEW) {
                    descriptionStr = descriptionStr + " (" + constants.previewModelLabel() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                    description = templates.snapshotDescription("color:orange", descriptionStr); //$NON-NLS-1$
                }
                else if (snapshot.getType() == SnapshotType.STATELESS) {
                    descriptionStr = descriptionStr + " (" + constants.readonlyLabel() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                    description = templates.snapshotDescription("font-style:italic", descriptionStr); //$NON-NLS-1$
                }
                else if (snapshot.getType() == SnapshotType.ACTIVE || snapshot.getType() == SnapshotType.PREVIEW) {
                    description = templates.snapshotDescription("color:gray", descriptionStr); //$NON-NLS-1$
                }

                return description;
            }
        };
        getTable().addColumn(descriptionColumn, constants.descriptionSnapshot(), "185px"); //$NON-NLS-1$

        initActionButtons(constants);
        disableActiveSnapshotRow();

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
            public String getCustomToolTip() {
                if (!getModel().getIsCloneVmSupported() && getModel().getEntity() != null) {
                    CommandVersionsInfo commandVersionsInfo =
                            AsyncDataProvider.getCommandVersionsInfo(VdcActionType.AddVmFromSnapshot);
                    String minimalClusterVersion = commandVersionsInfo != null ?
                            commandVersionsInfo.getClusterVersion().toString(2) : ""; //$NON-NLS-1$
                    return messages.cloneVmNotSupported(minimalClusterVersion);
                }
                else {
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
