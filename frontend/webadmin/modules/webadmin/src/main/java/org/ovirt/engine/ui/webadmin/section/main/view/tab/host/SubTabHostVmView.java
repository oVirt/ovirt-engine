package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostVmListModel;
import org.ovirt.engine.ui.webadmin.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.PercentColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.webadmin.widget.table.column.UptimeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmTypeColumn;

import com.google.gwt.core.client.GWT;

public class SubTabHostVmView extends AbstractSubTabTableView<VDS, VM, HostListModel, HostVmListModel>
        implements SubTabHostVmPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabHostVmView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabHostVmView(SearchableDetailModelProvider<VM, HostListModel, HostVmListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().addColumn(new VmStatusColumn(), "", "30px");

        TextColumnWithTooltip<VM> nameColumn = new TextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getvm_name();
            }
        };
        getTable().addColumn(nameColumn, "Name");

        getTable().addColumn(new VmTypeColumn(), "", "30px");

        TextColumnWithTooltip<VM> clusterColumn = new TextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getvds_group_name();
            }
        };
        getTable().addColumn(clusterColumn, "Cluster");

        TextColumnWithTooltip<VM> ipColumn = new TextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getvm_ip();
            }
        };
        getTable().addColumn(ipColumn, "IP Address");

        PercentColumn<VM> memColumn = new PercentColumn<VM>() {
            @Override
            public Integer getProgressValue(VM object) {
                return object.getusage_mem_percent();
            }
        };
        getTable().addColumn(memColumn, "Memory");

        PercentColumn<VM> cpuColumn = new PercentColumn<VM>() {
            @Override
            public Integer getProgressValue(VM object) {
                return object.getusage_cpu_percent();
            }
        };
        getTable().addColumn(cpuColumn, "CPU");

        PercentColumn<VM> netColumn = new PercentColumn<VM>() {
            @Override
            public Integer getProgressValue(VM object) {
                return object.getusage_network_percent();
            }
        };
        getTable().addColumn(netColumn, "Network");

        TextColumnWithTooltip<VM> statusColumn = new EnumColumn<VM, VMStatus>() {
            @Override
            protected VMStatus getRawValue(VM object) {
                return object.getstatus();
            }
        };
        getTable().addColumn(statusColumn, "Status");

        TextColumnWithTooltip<VM> hostColumn = new UptimeColumn<VM>() {
            @Override
            protected Double getRawValue(VM object) {
                return object.getRoundedElapsedTime();
            }
        };
        getTable().addColumn(hostColumn, "Uptime");

        getTable().addActionButton(new UiCommandButtonDefinition<VM>("Suspend") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getPauseCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<VM>("Shut down") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getShutdownCommand();
            }
        });
        // getTable().addActionButton(new UiCommandButtonDefinition<VM>(getListModel().getStopCommand(), "Stop"));
        // TODO: separator
        getTable().addActionButton(new UiCommandButtonDefinition<VM>("Migrate") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getMigrateCommand();
            }
        });
    }

}
