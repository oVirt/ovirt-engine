package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.action.CommandLocation;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostVmListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminImageButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.AbstractPercentColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.AbstractUptimeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmTypeColumn;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class SubTabHostVmView extends AbstractSubTabTableView<VDS, VM, HostListModel<Void>, HostVmListModel>
        implements SubTabHostVmPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabHostVmView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabHostVmView(SearchableDetailModelProvider<VM, HostListModel<Void>, HostVmListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable() {
        getTable().enableColumnResizing();

        VmStatusColumn<VM> statusIconColumn = new VmStatusColumn<>();
        statusIconColumn.setContextMenuTitle(constants.statusIconVm());
        getTable().addColumn(statusIconColumn, constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumn<VM> nameColumn = new AbstractTextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameVm(), "160px"); //$NON-NLS-1$

        VmTypeColumn typeColumn = new VmTypeColumn();
        typeColumn.setContextMenuTitle(constants.typeVm());
        getTable().addColumn(typeColumn, constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumn<VM> clusterColumn = new AbstractTextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getClusterName();
            }
        };
        clusterColumn.makeSortable();
        getTable().addColumn(clusterColumn, constants.clusterVm(), "160px"); //$NON-NLS-1$

        AbstractTextColumn<VM> ipColumn = new AbstractTextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getVmIp();
            }
        };
        ipColumn.makeSortable();
        getTable().addColumn(ipColumn, constants.ipVm(), "200px"); //$NON-NLS-1$

        AbstractTextColumn<VM> fqdnColumn = new AbstractTextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getVmFQDN();
            }
        };
        fqdnColumn.makeSortable();
        getTable().addColumn(fqdnColumn, constants.fqdn(), "200px"); //$NON-NLS-1$

        AbstractPercentColumn<VM> memColumn = new AbstractPercentColumn<VM>() {
            @Override
            public Integer getProgressValue(VM object) {
                return object.getUsageMemPercent();
            }
        };
        memColumn.makeSortable();
        getTable().addColumn(memColumn, constants.memoryVm(), "120px"); //$NON-NLS-1$

        AbstractPercentColumn<VM> cpuColumn = new AbstractPercentColumn<VM>() {
            @Override
            public Integer getProgressValue(VM object) {
                return object.getUsageCpuPercent();
            }
        };
        cpuColumn.makeSortable();
        getTable().addColumn(cpuColumn, constants.cpuVm(), "120px"); //$NON-NLS-1$

        AbstractPercentColumn<VM> netColumn = new AbstractPercentColumn<VM>() {
            @Override
            public Integer getProgressValue(VM object) {
                return object.getUsageNetworkPercent();
            }
        };
        netColumn.makeSortable();
        getTable().addColumn(netColumn, constants.networkVm(), "120px"); //$NON-NLS-1$

        AbstractTextColumn<VM> statusColumn = new AbstractEnumColumn<VM, VMStatus>() {
            @Override
            protected VMStatus getRawValue(VM object) {
                // check, if the current host is a target for the migration, then override status
                final VDS vds = getDetailModel().getEntity();
                if (object.getStatus().equals(VMStatus.MigratingFrom) && vds.getId().equals(object.getMigratingToVds())) {
                    return VMStatus.MigratingTo;
                }

                return object.getStatus();
            }
        };
        statusColumn.makeSortable();
        getTable().addColumn(statusColumn, constants.statusVm(), "130px"); //$NON-NLS-1$

        AbstractTextColumn<VM> uptimeColumn = new AbstractUptimeColumn<VM>() {
            @Override
            protected Double getRawValue(VM object) {
                return object.getRoundedElapsedTime();
            }
        };
        uptimeColumn.makeSortable();
        getTable().addColumn(uptimeColumn, constants.uptimeVm(), "110px"); //$NON-NLS-1$

        // add action buttons
        getTable().addActionButton(new WebAdminImageButtonDefinition<VM>(constants.suspendVm(),
                resources.suspendVmImage(), resources.suspendVmDisabledImage()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getPauseCommand();
            }

            @Override
            public SafeHtml getTooltip() {
                return SafeHtmlUtils.fromSafeConstant(constants.suspendVm());
            }
        });

        getTable().addActionButton(new WebAdminImageButtonDefinition<VM>(constants.shutDownVm(),
                resources.stopVmImage(), resources.stopVmDisabledImage()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getShutdownCommand();
            }

            @Override
            public SafeHtml getTooltip() {
                return SafeHtmlUtils.fromSafeConstant(constants.shutDownVm());
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<VM>(constants.powerOffVm(), CommandLocation.OnlyFromContext) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getStopCommand();
            }

            @Override
            public SafeHtml getTooltip() {
                return SafeHtmlUtils.fromSafeConstant(constants.powerOffVm());
            }
        });

        getTable().addActionButton(new WebAdminImageButtonDefinition<VM>(constants.consoleVm(),
                resources.consoleImage(), resources.consoleDisabledImage()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getConsoleConnectCommand();
            }

            @Override
            public SafeHtml getTooltip() {
                return SafeHtmlUtils.fromSafeConstant(constants.consoleVm());
            }

        });

        // TODO: separator

        getTable().addActionButton(new WebAdminButtonDefinition<VM>(constants.migrateVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getMigrateCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<VM>(constants.cancelMigrationVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getCancelMigrateCommand();
            }
        });
    }

}
