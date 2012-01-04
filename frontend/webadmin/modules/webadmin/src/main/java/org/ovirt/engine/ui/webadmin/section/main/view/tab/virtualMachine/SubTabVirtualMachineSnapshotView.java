package org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.vms.SnapshotModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSnapshotListModel;
import org.ovirt.engine.ui.webadmin.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineSnapshotPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.ActionCellTable;
import org.ovirt.engine.ui.webadmin.widget.table.OrderedMultiSelectionModel;
import org.ovirt.engine.ui.webadmin.widget.table.column.SafeHtmlColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.TextColumnWithTooltip;

import com.google.gwt.core.client.GWT;
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
import com.google.inject.Inject;

public class SubTabVirtualMachineSnapshotView extends AbstractSubTabTableView<VM, SnapshotModel, VmListModel, VmSnapshotListModel> implements SubTabVirtualMachineSnapshotPresenter.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, SubTabVirtualMachineSnapshotView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<SubTabVirtualMachineSnapshotView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    ActionCellTable<String> applicationsTable;

    @UiField
    HorizontalPanel mainContainer;

    @UiField
    SimplePanel snapshotsTableContainer;

    @UiField
    SimplePanel applicationsTableContainer;

    @Inject
    public SubTabVirtualMachineSnapshotView(SearchableDetailModelProvider<SnapshotModel, VmListModel, VmSnapshotListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initSnapshotsTable();
        initApplicationsTable();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        snapshotsTableContainer.add(getTable());
        applicationsTableContainer.add(applicationsTable);

        mainContainer.setCellWidth(snapshotsTableContainer, "50%");
        mainContainer.setCellWidth(applicationsTableContainer, "50%");
    }

    private void initSnapshotsTable() {
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

        getTable().addActionButton(new UiCommandButtonDefinition<SnapshotModel>("Create") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getNewCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<SnapshotModel>("Preview") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getPreviewCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<SnapshotModel>("Commit") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getCommitCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<SnapshotModel>("Undo") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getUndoCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<SnapshotModel>("Delete") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

    private void initApplicationsTable() {
        applicationsTable = new ActionCellTable<String>(new AbstractDataProvider<String>() {
            @Override
            protected void onRangeChanged(HasData<String> display) {
            }
        }, GWT.<Resources> create(SubTableResources.class));

        TextColumnWithTooltip<String> nameColumn = new TextColumnWithTooltip<String>() {
            @Override
            public String getValue(String object) {
                return object;
            }
        };

        applicationsTable.addColumn(nameColumn, "Installed Applications");
        applicationsTable.setWidth("100%");
        applicationsTable.setRowData(new ArrayList<String>());

        getDetailModel().getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                PropertyChangedEventArgs pcArgs = (PropertyChangedEventArgs) args;

                if ("Apps".equals(pcArgs.PropertyName)) {
                    applicationsTable.setRowData(Linq.ToList(getDetailModel().getApps()));
                }
            }
        });
    }

}
