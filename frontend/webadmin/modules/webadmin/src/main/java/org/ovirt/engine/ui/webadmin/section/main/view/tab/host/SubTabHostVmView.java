package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.gwtbootstrap3.client.ui.constants.Styles;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.searchbackend.VmConditionFieldAutoCompleter;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.FragmentParams;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.ViewRadioGroup;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLinkColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostVmFilter;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostVmListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.table.column.AbstractUptimeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.ColumnResizeTableLineChartProgressBar;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmStatusIconColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmTypeColumn;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;


public class SubTabHostVmView extends AbstractSubTabTableView<VDS, VM, HostListModel<Void>, HostVmListModel>
        implements SubTabHostVmPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabHostVmView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private ViewRadioGroup<HostVmFilter> viewRadioGroup;

    private AbstractTextColumn<VM> attachmentToCurHostColumn;

    @Inject
    public SubTabHostVmView(SearchableDetailModelProvider<VM, HostListModel<Void>, HostVmListModel> modelProvider) {
        super(modelProvider);

        initTable();
        getTable().setTableOverhead(createOverheadPanel());
        onFilterChange(viewRadioGroup.getSelectedValue());
        initWidget(getTableContainer());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    private FlowPanel createOverheadPanel() {
        Label label = new Label();
        label.setText(constants.vmFilters() + ":"); //$NON-NLS-1$
        label.addStyleName(Styles.PULL_LEFT);
        label.getElement().getStyle().setMarginTop(3, Style.Unit.PX);
        label.getElement().getStyle().setMarginRight(5, Style.Unit.PX);

        viewRadioGroup = new ViewRadioGroup<>(Arrays.asList(HostVmFilter.values()));
        viewRadioGroup.setSelectedValue(HostVmFilter.RUNNING_ON_CURRENT_HOST);
        viewRadioGroup.addChangeHandler(selection -> onFilterChange(selection));

        FlowPanel overheadPanel = new FlowPanel();
        overheadPanel.add(label);
        overheadPanel.add(viewRadioGroup);

        return overheadPanel;
    }

    void initTable() {
        getTable().enableColumnResizing();

        VmStatusIconColumn<VM> statusIconColumn = new VmStatusIconColumn<>();
        statusIconColumn.setContextMenuTitle(constants.statusIconVm());
        getTable().addColumn(statusIconColumn, constants.empty(), "35px"); //$NON-NLS-1$

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
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameVm(), "160px"); //$NON-NLS-1$

        VmTypeColumn typeColumn = new VmTypeColumn();
        typeColumn.setContextMenuTitle(constants.typeVm());
        getTable().addColumn(typeColumn, constants.empty(), "60px"); //$NON-NLS-1$

        attachmentToCurHostColumn = new AbstractTextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                Guid vdsId = getDetailModel().getEntity() != null? getDetailModel().getEntity().getId() : null;
                // if no hosts is set for the model yet, return an empty status.
                if (vdsId == null) {
                    return constants.empty();
                }
                boolean pinned = object.getDedicatedVmForVdsList() != null ? object.getDedicatedVmForVdsList().contains(vdsId) : false;
                boolean running = object.getRunOnVds() != null? object.getRunOnVds().equals(vdsId) : false;
                return pinned ? (running? constants.runningAndPinnedOnCurHost() : constants.pinnedToCurHost()) : constants.runningOnCurHost();
            }
        };
        attachmentToCurHostColumn.makeSortable();
        getTable().addColumn(attachmentToCurHostColumn, constants.attachmentToCurHost(), "200px"); //$NON-NLS-1$

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
                return object.getIp();
            }
        };
        ipColumn.makeSortable();
        getTable().addColumn(ipColumn, constants.ipVm(), "200px"); //$NON-NLS-1$

        AbstractTextColumn<VM> fqdnColumn = new AbstractTextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getFqdn();
            }
        };
        fqdnColumn.makeSortable();
        getTable().addColumn(fqdnColumn, constants.fqdn(), "200px"); //$NON-NLS-1$

        getTable().addColumn(new ColumnResizeTableLineChartProgressBar<VDS, VM>(
                getTable(),
                new ResourceConsumptionComparator() {

                    @Override
                    protected Integer extractValue(VM vm) {
                        return vm.getUsageMemPercent();
                    }
                }) {
            @Override
            protected List<Integer> getProgressValues(VM object) {
                return object.getMemoryUsageHistory();
            }
        }, constants.memoryVm(), "120px"); //$NON-NLS-1$

        getTable().addColumn(new ColumnResizeTableLineChartProgressBar<VDS, VM>(
                getTable(),
                new ResourceConsumptionComparator() {
                    @Override
                    protected Integer extractValue(VM vm) {
                        return vm.getUsageCpuPercent();
                    }
                }) {
            @Override
            protected List<Integer> getProgressValues(VM object) {
                return object.getCpuUsageHistory();
            }
        }, constants.cpuVm(), "120px"); //$NON-NLS-1$

        getTable().addColumn(new ColumnResizeTableLineChartProgressBar<VDS, VM>(
                getTable(),
                new ResourceConsumptionComparator() {
                    @Override
                    protected Integer extractValue(VM vm) {
                        return vm.getUsageNetworkPercent();
                    }
                }) {
            @Override
            protected List<Integer> getProgressValues(VM object) {
                return object.getNetworkUsageHistory();
            }
        }, constants.networkVm(), "120px"); //$NON-NLS-1$

        VmStatusColumn statusColumn = new VmStatusColumn(() ->  {
            VDS host = getDetailModel().getEntity();
            return host != null ? host.getId() : null;
        });
        statusColumn.makeSortable(VmConditionFieldAutoCompleter.STATUS);
        getTable().addColumn(statusColumn, constants.statusVm(), "130px"); //$NON-NLS-1$

        AbstractTextColumn<VM> uptimeColumn = new AbstractUptimeColumn<VM>() {
            @Override
            protected Double getRawValue(VM object) {
                return object.getElapsedTime();
            }
        };
        uptimeColumn.makeSortable();
        getTable().addColumn(uptimeColumn, constants.uptimeVm(), "110px"); //$NON-NLS-1$
    }

    private void onFilterChange(HostVmFilter selected) {
        getTable().ensureColumnVisible(attachmentToCurHostColumn, constants.attachmentToCurHost(), selected == HostVmFilter.BOTH);
        getDetailModel().setViewFilterType(selected);
    }

    abstract class ResourceConsumptionComparator implements Comparator<VM> {

        @Override
        public int compare(VM vm1, VM vm2) {
            Integer val1 = extractValue(vm1) != null ? extractValue(vm1) : 0;
            Integer val2 = extractValue(vm2) != null ? extractValue(vm2) : 0;
            return val1.compareTo(val2);
        }

        protected abstract Integer extractValue(VM vm);
    }
}
