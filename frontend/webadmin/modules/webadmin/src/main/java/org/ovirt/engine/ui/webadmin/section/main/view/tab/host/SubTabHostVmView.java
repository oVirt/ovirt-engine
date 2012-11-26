package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostVmListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.PercentColumn;
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
    public SubTabHostVmView(SearchableDetailModelProvider<VM, HostListModel, HostVmListModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(constants);
        initWidget(getTable());
    }

    void initTable(ApplicationConstants constants) {
        getTable().addColumn(new VmStatusColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        TextColumnWithTooltip<VM> nameColumn = new TextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getVmName();
            }
        };
        getTable().addColumn(nameColumn, constants.nameVm());

        getTable().addColumn(new VmTypeColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        TextColumnWithTooltip<VM> clusterColumn = new TextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getVdsGroupName();
            }
        };
        getTable().addColumn(clusterColumn, constants.clusterVm());

        TextColumnWithTooltip<VM> ipColumn = new TextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getVmIp();
            }
        };
        getTable().addColumn(ipColumn, constants.ipVm());

        PercentColumn<VM> memColumn = new PercentColumn<VM>() {
            @Override
            public Integer getProgressValue(VM object) {
                return object.getUsageMemPercent();
            }
        };
        getTable().addColumn(memColumn, constants.memoryVm());

        PercentColumn<VM> cpuColumn = new PercentColumn<VM>() {
            @Override
            public Integer getProgressValue(VM object) {
                return object.getUsageCpuPercent();
            }
        };
        getTable().addColumn(cpuColumn, constants.cpuVm());

        PercentColumn<VM> netColumn = new PercentColumn<VM>() {
            @Override
            public Integer getProgressValue(VM object) {
                return object.getUsageNetworkPercent();
            }
        };
        getTable().addColumn(netColumn, constants.networkVm());

        TextColumnWithTooltip<VM> statusColumn = new EnumColumn<VM, VMStatus>() {
            @Override
            protected VMStatus getRawValue(VM object) {
                return object.getStatus();
            }
        };
        getTable().addColumn(statusColumn, constants.statusVm());

        TextColumnWithTooltip<VM> hostColumn = new UptimeColumn<VM>() {
            @Override
            protected Double getRawValue(VM object) {
                return object.getRoundedElapsedTime();
            }
        };
        getTable().addColumn(hostColumn, constants.uptimeVm());

        getTable().addActionButton(new WebAdminButtonDefinition<VM>(constants.suspendVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getPauseCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<VM>(constants.shutDownVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getShutdownCommand();
            }
        });
        // getTable().addActionButton(new WebAdminButtonDefinition<VM>(getListModel().getStopCommand(), "Stop"));
        // TODO: separator
        getTable().addActionButton(new WebAdminButtonDefinition<VM>(constants.migrateVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getMigrateCommand();
            }
        });
    }

}
