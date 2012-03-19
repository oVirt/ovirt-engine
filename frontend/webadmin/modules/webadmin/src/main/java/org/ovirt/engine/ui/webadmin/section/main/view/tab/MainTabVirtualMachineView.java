package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.action.CommandLocation;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.ReportInit;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabVirtualMachinePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.uicommon.ReportActionsHelper;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminImageButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminMenuBarButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.PercentColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.UptimeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmTypeColumn;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class MainTabVirtualMachineView extends AbstractMainTabWithDetailsTableView<VM, VmListModel> implements MainTabVirtualMachinePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabVirtualMachineView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public MainTabVirtualMachineView(MainModelProvider<VM, VmListModel> modelProvider,
            ApplicationResources resources) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(resources);
        initWidget(getTable());
    }

    void initTable(ApplicationResources resources) {
        getTable().addColumn(new VmStatusColumn(), "", "30px");

        TextColumnWithTooltip<VM> nameColumn = new TextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getvm_name();
            }
        };
        getTable().addColumn(nameColumn, "Name", "150px");

        getTable().addColumn(new VmTypeColumn(), "", "40px");

        TextColumnWithTooltip<VM> clusterColumn = new TextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getvds_group_name();
            }
        };
        getTable().addColumn(clusterColumn, "Cluster", "100px");

        TextColumnWithTooltip<VM> hostColumn = new TextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getrun_on_vds_name();
            }
        };
        getTable().addColumn(hostColumn, "Host", "100px");

        TextColumnWithTooltip<VM> ipColumn = new TextColumnWithTooltip<VM>() {
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

        TextColumnWithTooltip<VM> displayColumn = new EnumColumn<VM, DisplayType>() {
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

        TextColumnWithTooltip<VM> statusColumn = new EnumColumn<VM, VMStatus>() {
            @Override
            public VMStatus getRawValue(VM object) {
                return object.getstatus();
            }
        };
        getTable().addColumn(statusColumn, "Status", "90px");

        TextColumnWithTooltip<VM> uptimeColumn = new UptimeColumn<VM>() {
            @Override
            public Double getRawValue(VM object) {
                return object.getRoundedElapsedTime();
            }
        };
        getTable().addColumn(uptimeColumn, "Uptime", "70px");

        TextColumnWithTooltip<VM> loggedInUserColumn = new TextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                if (object.getguest_cur_user_name() == null) {
                    return "";
                }
                return String.valueOf(object.getguest_cur_user_name());
            }
        };
        getTable().addColumn(loggedInUserColumn, "Logged-in User", "90px");

        getTable().addActionButton(new WebAdminButtonDefinition<VM>("New Server") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getNewServerCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<VM>("New Desktop") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getNewDesktopCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<VM>("Edit") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getEditCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<VM>("Remove") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRemoveCommand();
            }
        });
        // TODO: separator
        getTable().addActionButton(new WebAdminButtonDefinition<VM>("Run Once") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRunOnceCommand();
            }
        });
        getTable().addActionButton(new WebAdminImageButtonDefinition<VM>("Run",
                resources.runVmImage(), resources.runVmDisabledImage()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRunCommand();
            }
        });
        getTable().addActionButton(new WebAdminImageButtonDefinition<VM>("Suspend",
                resources.pauseVmImage(), resources.pauseVmDisabledImage()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getPauseCommand();
            }
        });
        getTable().addActionButton(new WebAdminImageButtonDefinition<VM>("Shut down",
                resources.stopVmImage(), resources.stopVmDisabledImage()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getShutdownCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<VM>("Stop", CommandLocation.OnlyFromFromContext) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getStopCommand();
            }
        });
        // TODO: separator
        getTable().addActionButton(new WebAdminImageButtonDefinition<VM>("Console",
                resources.consoleImage(), resources.consoleDisabledImage()) {
            @Override
            protected UICommand resolveCommand() {
                ConsoleModel defaultConsoleModel = getMainModel().getDefaultConsoleModel();
                return defaultConsoleModel != null ? defaultConsoleModel.getConnectCommand() : null;
            }
        });
        // TODO: separator
        getTable().addActionButton(new WebAdminButtonDefinition<VM>("Migrate") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getMigrateCommand();
            }
        });
        // TODO: separator
        getTable().addActionButton(new WebAdminButtonDefinition<VM>("Cancel Migration") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getCancelMigrateCommand();
            }
        });
        // TODO: separator
        getTable().addActionButton(new WebAdminButtonDefinition<VM>("Make Template") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getNewTemplateCommand();
            }
        });
        // TODO: separator
        getTable().addActionButton(new WebAdminButtonDefinition<VM>("Export") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getExportCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<VM>("Move") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getMoveCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<VM>("Change CD") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getChangeCdCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<VM>("Assign Tags") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getAssignTagsCommand();
            }
        });

        if (ReportInit.getInstance().isReportsEnabled()) {
            List<ActionButtonDefinition<VM>> resourceSubActions =
                    ReportActionsHelper.getInstance().getResourceSubActions("VM", getMainModel());
            if (resourceSubActions != null && resourceSubActions.size() > 0) {
                getTable().addActionButton(new WebAdminMenuBarButtonDefinition<VM>("Show Report",
                        resourceSubActions));
            }
        }

        getTable().addActionButton(new WebAdminImageButtonDefinition<VM>("Guide Me",
                resources.guideSmallImage(), resources.guideSmallDisabledImage(), true) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getGuideCommand();
            }
        });
    }
}
