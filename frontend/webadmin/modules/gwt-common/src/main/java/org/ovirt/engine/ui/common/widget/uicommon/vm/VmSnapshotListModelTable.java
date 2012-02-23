package org.ovirt.engine.ui.common.widget.uicommon.vm;

import java.util.ArrayList;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.common.SubTableResources;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.common.widget.table.ActionCellTable;
import org.ovirt.engine.ui.common.widget.table.OrderedMultiSelectionModel;
import org.ovirt.engine.ui.common.widget.table.column.SafeHtmlColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.vms.SnapshotModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSnapshotListModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.DefaultSelectionEventManager.EventTranslator;
import com.google.gwt.view.client.DefaultSelectionEventManager.SelectAction;
import com.google.gwt.view.client.HasData;

public class VmSnapshotListModelTable extends AbstractModelBoundTableWidget<SnapshotModel, VmSnapshotListModel> {

    interface WidgetUiBinder extends UiBinder<Widget, VmSnapshotListModelTable> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private final ActionCellTable<String> applicationsTable;

    @UiField
    HorizontalPanel mainContainer;

    @UiField
    SimplePanel snapshotsTableContainer;

    @UiField
    SimplePanel applicationsTableContainer;

    public VmSnapshotListModelTable(
            SearchableTableModelProvider<SnapshotModel, VmSnapshotListModel> modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage, false);
        this.applicationsTable = createApplicationsTable();
    }

    ActionCellTable<String> createApplicationsTable() {
        return new ActionCellTable<String>(new AbstractDataProvider<String>() {
            @Override
            protected void onRangeChanged(HasData<String> display) {
            }
        }, GWT.<Resources> create(SubTableResources.class));
    }

    @Override
    protected Widget getWrappedWidget() {
        Widget rootWidget = WidgetUiBinder.uiBinder.createAndBindUi(this);

        snapshotsTableContainer.add(getTable());
        applicationsTableContainer.add(applicationsTable);

        mainContainer.setCellWidth(snapshotsTableContainer, "50%");
        mainContainer.setCellWidth(applicationsTableContainer, "50%");

        return rootWidget;
    }

    @Override
    public void initTable() {
        // Create a selection event manager (to disable 'current' snapshot selection)
        DefaultSelectionEventManager<SnapshotModel> selectionEventManager =
                DefaultSelectionEventManager.createCustomManager(new EventTranslator<SnapshotModel>() {
                    @Override
                    public boolean clearCurrentSelection(CellPreviewEvent<SnapshotModel> event) {
                        return true;
                    }

                    @Override
                    public SelectAction translateSelectionEvent(CellPreviewEvent<SnapshotModel> event) {
                        if (event.getValue().getIsCurrent()) {
                            return SelectAction.IGNORE;
                        }

                        return SelectAction.DEFAULT;
                    }
                });

        // Set selection mode, disable multiselection and first row ('current' snapshot)
        OrderedMultiSelectionModel<SnapshotModel> selectionModel = getTable().getSelectionModel();
        selectionModel.setDisabledRows(0);
        getTable().setTableSelectionModel(selectionModel, selectionEventManager);
        getTable().setMultiSelectionDisabled(true);

        TextColumnWithTooltip<SnapshotModel> nameColumn = new TextColumnWithTooltip<SnapshotModel>() {
            @Override
            public String getValue(SnapshotModel object) {
                if (object.getDate() == null) {
                    return "Current";
                }

                DateTimeFormat format = DateTimeFormat.getFormat("yyyy-MMM-dd, HH:mm:ss");
                return format.format(object.getDate());
            }
        };
        getTable().addColumn(nameColumn, "Date");

        SafeHtmlColumn<SnapshotModel> descriptionColumn = new SafeHtmlColumn<SnapshotModel>() {
            @Override
            public final SafeHtml getValue(SnapshotModel object) {
                // Get raw description string (ignore < and > characters).
                // Customize description style as needed.
                String descriptionStr = SafeHtmlUtils.fromString(object.getDescriptionValue()).asString();
                if (object.getIsCurrent())
                    descriptionStr = "<b><font color=gray>" + descriptionStr + "</font></b>";
                if (object.getIsPreviewed())
                    descriptionStr = "<b><font color=orange>" + descriptionStr + "</font></b>";

                return SafeHtmlUtils.fromTrustedString(descriptionStr);
            }
        };
        getTable().addColumn(descriptionColumn, "Description");

        TextColumnWithTooltip<SnapshotModel> diskColumn = new TextColumnWithTooltip<SnapshotModel>() {
            @Override
            public String getValue(SnapshotModel object) {
                return object.getParticipantDisks();
            }
        };
        getTable().addColumn(diskColumn, "Disks");

        getTable().addActionButton(new UiCommandButtonDefinition<SnapshotModel>(getEventBus(), "Create") {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<SnapshotModel>(getEventBus(), "Preview") {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getPreviewCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<SnapshotModel>(getEventBus(), "Commit") {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getCommitCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<SnapshotModel>(getEventBus(), "Undo") {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getUndoCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<SnapshotModel>(getEventBus(), "Delete") {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });

        initApplicationsTable();
    }

    void initApplicationsTable() {
        TextColumnWithTooltip<String> nameColumn = new TextColumnWithTooltip<String>() {
            @Override
            public String getValue(String object) {
                return object;
            }
        };

        applicationsTable.addColumn(nameColumn, "Installed Applications");
        applicationsTable.setWidth("100%");
        applicationsTable.setRowData(new ArrayList<String>());

        getModel().getPropertyChangedEvent().addListener(new IEventListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                PropertyChangedEventArgs pcArgs = (PropertyChangedEventArgs) args;

                if ("Apps".equals(pcArgs.PropertyName)) {
                    applicationsTable.setRowData(Linq.ToList(getModel().getApps()));
                }
            }
        });
    }

}
