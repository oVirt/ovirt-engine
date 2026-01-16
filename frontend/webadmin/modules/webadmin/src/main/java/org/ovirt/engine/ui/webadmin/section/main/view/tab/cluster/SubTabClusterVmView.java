package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.searchbackend.VmConditionFieldAutoCompleter;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.FragmentParams;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLinkColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterVmListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.table.column.AbstractUptimeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.ColumnResizeTableLineChartProgressBar;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmStatusIconColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmTypeColumn;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;

public class SubTabClusterVmView extends AbstractSubTabTableView<Cluster, VM, ClusterListModel<Void>, ClusterVmListModel>
        implements SubTabClusterVmPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabClusterVmView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabClusterVmView(SearchableDetailModelProvider<VM, ClusterListModel<Void>, ClusterVmListModel> modelProvider) {
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

        VmStatusIconColumn<VM> statusIconColumn = new VmStatusIconColumn<>();
        statusIconColumn.setContextMenuTitle(constants.statusIconVm());
        statusIconColumn.makeSortable(VmConditionFieldAutoCompleter.STATUS);
        getTable().addColumn(statusIconColumn, constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumn<VM> nameColumn = new AbstractLinkColumn<VM>(new FieldUpdater<VM, String>() {
            @Override
            public void update(int index, VM vm, String value) {
                Map<String, String> parameters = new HashMap<>();
                parameters.put(FragmentParams.NAME.getName(), vm.getName());
                getPlaceTransitionHandler().handlePlaceTransition(
                        WebAdminApplicationPlaces.virtualMachineGeneralSubTabPlace, parameters);
            }
        }) {
            @Override
            public String getValue(VM object) {
                return object.getName();
            }
        };

        nameColumn.makeSortable(VmConditionFieldAutoCompleter.NAME);
        getTable().addColumn(nameColumn, constants.nameVm(), "220px"); //$NON-NLS-1$

        VmTypeColumn typeColumn = new VmTypeColumn();
        typeColumn.setContextMenuTitle(constants.typeVm());
        typeColumn.makeSortable(VmConditionFieldAutoCompleter.TYPE);
        getTable().addColumn(typeColumn, constants.empty(), "60px"); //$NON-NLS-1$

        AbstractTextColumn<VM> statusColumn = new AbstractEnumColumn<VM, VMStatus>() {
            @Override
            protected VMStatus getRawValue(VM object) {
                return object.getStatus();
            }
        };
        statusColumn.makeSortable(VmConditionFieldAutoCompleter.STATUS);
        getTable().addColumn(statusColumn, constants.statusVm(), "220px"); //$NON-NLS-1$

        AbstractTextColumn<VM> uptimeColumn = new AbstractUptimeColumn<VM>() {
            @Override
            protected Double getRawValue(VM object) {
                return object.getElapsedTime();
            }
        };
        uptimeColumn.makeSortable(VmConditionFieldAutoCompleter.UPTIME);
        getTable().addColumn(uptimeColumn, constants.uptimeVm(), "220px"); //$NON-NLS-1$

        getTable().addColumn(new ColumnResizeTableLineChartProgressBar<Cluster, VM>(
                getTable(),
                VmConditionFieldAutoCompleter.CPU_USAGE) {
            @Override
            protected List<Integer> getProgressValues(VM object) {
                return object.getCpuUsageHistory();
            }
        }, constants.cpuVm(), "80px"); //$NON-NLS-1$

        getTable().addColumn(new ColumnResizeTableLineChartProgressBar<Cluster, VM>(
                getTable(),
                VmConditionFieldAutoCompleter.MEM_USAGE) {
            @Override
            protected List<Integer> getProgressValues(VM object) {
                return object.getMemoryUsageHistory();
            }
        }, constants.memoryVm(), "80px"); //$NON-NLS-1$

        getTable().addColumn(new ColumnResizeTableLineChartProgressBar<Cluster, VM>(
                getTable(),
                VmConditionFieldAutoCompleter.NETWORK_USAGE) {
            @Override
            protected List<Integer> getProgressValues(VM object) {
                return object.getNetworkUsageHistory();
            }
        }, constants.networkVm(), "80px"); //$NON-NLS-1$

        AbstractTextColumn<VM> ipColumn = new AbstractTextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getIp();
            }
        };
        ipColumn.makeSortable(VmConditionFieldAutoCompleter.IP);
        getTable().addColumn(ipColumn, constants.ipVm(), "120px"); //$NON-NLS-1$

        AbstractTextColumn<VM> vCpusColumn = new AbstractTextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return Integer.toString(object.getNumOfCpus());
            }
        };
        getTable().addColumn(vCpusColumn, constants.vcpus(), "80px"); //$NON-NLS-1$
        vCpusColumn.makeSortable(VmConditionFieldAutoCompleter.VCPUS);
        getTable().hideColumnByDefault(vCpusColumn);

        AbstractTextColumn<VM> definedMemoryColumn = new AbstractTextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getMemSizeMb() + " MB"; //$NON-NLS-1$
            }
        };
        getTable().addColumn(definedMemoryColumn, constants.memoryVmMB(), "100px"); //$NON-NLS-1$
        definedMemoryColumn.makeSortable(VmConditionFieldAutoCompleter.MEMORY);
        getTable().hideColumnByDefault(definedMemoryColumn);
    }
}
