package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabHostPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.HostStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.PercentColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.ProgressBarColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.TextColumnWithTooltip;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class MainTabHostView extends AbstractMainTabWithDetailsTableView<VDS, HostListModel> implements MainTabHostPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabHostView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public MainTabHostView(MainModelProvider<VDS, HostListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().addColumn(new HostStatusColumn(), "", "30px");

        TextColumnWithTooltip<VDS> nameColumn = new TextColumnWithTooltip<VDS>() {
            @Override
            public String getValue(VDS object) {
                return object.getvds_name();
            }
        };
        getTable().addColumn(nameColumn, "Name");

        TextColumnWithTooltip<VDS> hostColumn = new TextColumnWithTooltip<VDS>() {
            @Override
            public String getValue(VDS object) {
                return object.gethost_name();
            }
        };
        getTable().addColumn(hostColumn, "Host/IP");

        TextColumnWithTooltip<VDS> clusterColumn = new TextColumnWithTooltip<VDS>() {
            @Override
            public String getValue(VDS object) {
                return object.getvds_group_name();
            }
        };
        getTable().addColumn(clusterColumn, "Cluster");

        TextColumnWithTooltip<VDS> statusColumn = new EnumColumn<VDS, VDSStatus>() {
            @Override
            public VDSStatus getRawValue(VDS object) {
                return object.getstatus();
            }
        };
        getTable().addColumn(statusColumn, "Status");

        ProgressBarColumn<VDS> loadColumn = new ProgressBarColumn<VDS>() {
            @Override
            protected String getProgressText(VDS object) {
                int numOfActiveVMs = object.getvm_active() != null ? object.getvm_active() : 0;
                return numOfActiveVMs + " VMs";
            }

            @Override
            protected Integer getProgressValue(VDS object) {
                return object.getvm_active();
            }
        };
        getTable().addColumn(loadColumn, "Load", "100px");

        PercentColumn<VDS> memColumn = new PercentColumn<VDS>() {
            @Override
            public Integer getProgressValue(VDS object) {
                return object.getusage_mem_percent();
            }
        };
        getTable().addColumn(memColumn, "Memory", "60px");

        PercentColumn<VDS> cpuColumn = new PercentColumn<VDS>() {
            @Override
            public Integer getProgressValue(VDS object) {
                return object.getusage_cpu_percent();
            }
        };
        getTable().addColumn(cpuColumn, "CPU", "60px");

        PercentColumn<VDS> netColumn = new PercentColumn<VDS>() {
            @Override
            public Integer getProgressValue(VDS object) {
                return object.getusage_network_percent();
            }
        };
        getTable().addColumn(netColumn, "Network", "60px");

        TextColumnWithTooltip<VDS> spmColumn = new EnumColumn<VDS, VdsSpmStatus>() {
            @Override
            public VdsSpmStatus getRawValue(VDS object) {
                return object.getspm_status();
            }
        };
        getTable().addColumn(spmColumn, "SpmStatus");

        getTable().addActionButton(new UiCommandButtonDefinition<VDS>("New") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getNewCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<VDS>("Edit") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getEditCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<VDS>("Remove") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRemoveCommand();
            }
        });
        // TODO: separator
        getTable().addActionButton(new UiCommandButtonDefinition<VDS>("Activate") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getActivateCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<VDS>("Maintenance") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getMaintenanceCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<VDS>("Confirm 'Host has been Rebooted'", true) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getManualFenceCommand();
            }
        });
        // TODO: separator
        getTable().addActionButton(new UiCommandButtonDefinition<VDS>("Approve") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getApproveCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<VDS>("Configure Local Storage") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getConfigureLocalStorageCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<VDS>("Assign Tags") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getAssignTagsCommand();
            }
        });
    }

}
