package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabVirtualMachinePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.DynamicUiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.ImageUiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.PercentColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.UptimeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmTypeColumn;

import com.google.gwt.user.cellview.client.TextColumn;
import com.google.inject.Inject;

public class MainTabVirtualMachineView extends AbstractMainTabWithDetailsTableView<VM, VmListModel> implements MainTabVirtualMachinePresenter.ViewDef {

    @Inject
    public MainTabVirtualMachineView(MainModelProvider<VM, VmListModel> modelProvider,
            ApplicationResources resources) {
        super(modelProvider);
        initTable(resources);
        initWidget(getTable());
    }

    void initTable(ApplicationResources resources) {
        getTable().addColumn(new VmStatusColumn(), "", "30px");

        TextColumn<VM> nameColumn = new TextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getvm_name();
            }
        };
        getTable().addColumn(nameColumn, "Name", "150px");

        getTable().addColumn(new VmTypeColumn(), "", "40px");

        TextColumn<VM> clusterColumn = new TextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getvds_group_name();
            }
        };

        getTable().addColumn(clusterColumn, "Cluster", "100px");

        TextColumn<VM> hostColumn = new TextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getrun_on_vds_name();
            }
        };

        getTable().addColumn(hostColumn, "Host", "100px");

        TextColumn<VM> ipColumn = new TextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getvm_ip();
            }
        };

        getTable().addColumn(ipColumn, "IP Address", "100px");

        PercentColumn<VM> memoryColumn = new PercentColumn<VM>() {
            @Override
            public Integer getProgressValue(VM object) {
                return object.getusage_mem_percent();
            }
        };

        getTable().addColumn(memoryColumn, "Memory", "60px");

        PercentColumn<VM> cpuColumn = new PercentColumn<VM>() {
            @Override
            public Integer getProgressValue(VM object) {
                return object.getusage_cpu_percent();
            }
        };
        getTable().addColumn(cpuColumn, "CPU", "60px");

        PercentColumn<VM> networkColumn = new PercentColumn<VM>() {
            @Override
            public Integer getProgressValue(VM object) {
                return object.getusage_network_percent();
            }
        };
        getTable().addColumn(networkColumn, "Network", "60px");

        TextColumn<VM> displayColumn = new EnumColumn<VM, DisplayType>() {
            @Override
            protected DisplayType getRawValue(VM object) {
                return object.getdisplay_type();
            }
            
            @Override
            public String getValue(VM object) {
                if ((object.getstatus() == VMStatus.Down) || (object.getstatus() == VMStatus.ImageLocked))
                    return "";
                else
                    return renderer.render(getRawValue(object));
            }
        };
        getTable().addColumn(displayColumn, "Display", "70px");

        TextColumn<VM> statusColumn = new EnumColumn<VM, VMStatus>() {
            @Override
            public VMStatus getRawValue(VM object) {
                return object.getstatus();
            }
        };
        getTable().addColumn(statusColumn, "Status", "90px");

        TextColumn<VM> uptimeColumn = new UptimeColumn<VM>() {
            @Override
            public Double getRawValue(VM object) {
                return object.getRoundedElapsedTime();
            }
        };
        getTable().addColumn(uptimeColumn, "Uptime", "70px");

        TextColumn<VM> loggedInUserColumn = new TextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                if (object.getguest_cur_user_name() == null) {
                    return "";
                }
                return String.valueOf(object.getguest_cur_user_name());
            }
        };
        getTable().addColumn(loggedInUserColumn, "Logged-in User", "90px");

        // Action BUttons
        getTable().addActionButton(new UiCommandButtonDefinition<VM>(getMainModel().getNewServerCommand(), "New Server"));
        getTable().addActionButton(new UiCommandButtonDefinition<VM>(getMainModel().getNewDesktopCommand(),
                "New Desktop"));
        getTable().addActionButton(new UiCommandButtonDefinition<VM>(getMainModel().getEditCommand(), "Edit"));
        getTable().addActionButton(new UiCommandButtonDefinition<VM>(getMainModel().getRemoveCommand(), "Remove"));
        // TODO: separator
        getTable().addActionButton(new UiCommandButtonDefinition<VM>(getMainModel().getRunOnceCommand(), "Run Once"));
        getTable().addActionButton(new ImageUiCommandButtonDefinition<VM>(getMainModel().getRunCommand(), "Run",
                resources.runVmImage(), resources.runVmDisabledImage()));
        getTable().addActionButton(new ImageUiCommandButtonDefinition<VM>(getMainModel().getPauseCommand(), "Suspend",
                resources.pauseVmImage(), resources.pauseVmDisabledImage()));
        getTable().addActionButton(new ImageUiCommandButtonDefinition<VM>(getMainModel().getShutdownCommand(),
                "Shut down", resources.stopVmImage(), resources.stopVmDisabledImage()));
        getTable().addActionButton(new UiCommandButtonDefinition<VM>(getMainModel().getStopCommand(), "Stop", true));
        // TODO: separator
        getTable().addActionButton(new DynamicUiCommandButtonDefinition<VM, VmListModel>(getMainModel(), "Console",
                resources.consoleImage(), resources.consoleDisabledImage()) {
            @Override
            protected UICommand getUpdatedCommand(VmListModel target) {
                ConsoleModel defaultConsoleModel = target.getDefaultConsoleModel();
                return defaultConsoleModel == null ? null : defaultConsoleModel.getConnectCommand();
            }
        });
        // TODO: separator
        getTable().addActionButton(new UiCommandButtonDefinition<VM>(getMainModel().getMigrateCommand(),
                "Migrate", false, false));
        // TODO: separator
        getTable().addActionButton(new UiCommandButtonDefinition<VM>(getMainModel().getNewTemplateCommand(),
                "Make Template"));
        // TODO: separator
        getTable().addActionButton(new UiCommandButtonDefinition<VM>(getMainModel().getExportCommand(),
                "Export", false, false));
        getTable().addActionButton(new UiCommandButtonDefinition<VM>(getMainModel().getMoveCommand(),
                "Move", false, false));
        getTable().addActionButton(new UiCommandButtonDefinition<VM>(getMainModel().getChangeCdCommand(),
                "Change CD"));
        getTable().addActionButton(new UiCommandButtonDefinition<VM>(getMainModel().getAssignTagsCommand(),
                "Assign Tags"));
    }
}
