package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import java.util.ArrayList;
import java.util.Date;

import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.ActionCellTable;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.VmBackupModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageVmBackupPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.GeneralDateTimeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmStatusColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;

public class SubTabStorageVmBackupView extends AbstractSubTabTableView<storage_domains, VM, StorageListModel, VmBackupModel>
        implements SubTabStorageVmBackupPresenter.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, SubTabStorageVmBackupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    HorizontalPanel mainContainer;

    @UiField
    SimplePanel vmTableContainer;

    @UiField
    SimplePanel applicationsTableContainer;

    ActionCellTable<String> applicationsTable;

    @Inject
    public SubTabStorageVmBackupView(SearchableDetailModelProvider<VM, StorageListModel, VmBackupModel> modelProvider) {
        super(modelProvider);
        initVmTable();
        initApplicationsTable();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        vmTableContainer.add(getTable());
        applicationsTableContainer.add(applicationsTable);

        mainContainer.setCellWidth(vmTableContainer, "50%");
        mainContainer.setCellWidth(applicationsTableContainer, "50%");
    }

    void initVmTable() {

        getTable().addColumn(new VmStatusColumn(), "", "30px");

        TextColumnWithTooltip<VM> nameColumn = new TextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getvm_name();
            }
        };
        getTable().addColumn(nameColumn, "Name");

        TextColumnWithTooltip<VM> templateColumn = new TextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getvmt_name();
            }
        };
        getTable().addColumn(templateColumn, "Template");

        TextColumnWithTooltip<VM> originColumn = new EnumColumn<VM, OriginType>() {
            @Override
            protected OriginType getRawValue(VM object) {
                return object.getorigin();
            }
        };
        getTable().addColumn(originColumn, "Origin");

        TextColumnWithTooltip<VM> memoryColumn = new TextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return String.valueOf(object.getvm_mem_size_mb()) + " MB";
            }
        };
        getTable().addColumn(memoryColumn, "Memory");

        TextColumnWithTooltip<VM> cpuColumn = new TextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return String.valueOf(object.getnum_of_cpus());
            }
        };
        getTable().addColumn(cpuColumn, "CPUs");

        TextColumnWithTooltip<VM> diskColumn = new TextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return String.valueOf(object.getDiskMap().size());
            }
        };
        getTable().addColumn(diskColumn, "Disks");

        TextColumnWithTooltip<VM> creationDateColumn = new GeneralDateTimeColumn<VM>() {
            @Override
            protected Date getRawValue(VM object) {
                return object.getvm_creation_date();
            }
        };
        getTable().addColumn(creationDateColumn, "Creation Date");

        getTable().addActionButton(new WebAdminButtonDefinition<VM>("Restore") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRestoreCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<VM>("Remove") {
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
            @SuppressWarnings("unchecked")
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (getDetailModel().getAppListModel().getItems() != null) {
                    applicationsTable.setRowData(Linq.ToList(getDetailModel().getAppListModel().getItems()));
                } else {
                    applicationsTable.setRowData(new ArrayList<String>());
                }
            }
        });
    }

}
