package org.ovirt.engine.ui.webadmin.section.main.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.searchbackend.VdsConditionFieldAutoCompleter;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.FragmentParams;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.common.widget.table.cell.Cell;
import org.ovirt.engine.ui.common.widget.table.cell.StatusCompositeCell;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLinkColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainHostPresenter;
import org.ovirt.engine.ui.webadmin.widget.table.column.AbstractPercentColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.CommentColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.HostAdditionalStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.HostStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.ReasonColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmCountColumn;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.inject.Inject;

public class MainHostView extends AbstractMainWithDetailsTableView<VDS, HostListModel<Void>> implements MainHostPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainHostView> {

        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    int maxSpmPriority;
    int defaultSpmPriority;

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final String NETWORKS_UPDATING = " - " + constants.networksUpdating() //$NON-NLS-1$
            + "..."; //$NON-NLS-1$

    @Inject
    public MainHostView(MainModelProvider<VDS, HostListModel<Void>> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);

        InitSpmPriorities();
    }

    private void InitSpmPriorities() {
        AsyncDataProvider.getInstance().getMaxSpmPriority(new AsyncQuery<>(returnValue -> {
            maxSpmPriority = returnValue;
            InitSpmPriorities1();
        }));
    }

    private void InitSpmPriorities1() {
        AsyncDataProvider.getInstance().getDefaultSpmPriority(new AsyncQuery<>(returnValue -> {
            defaultSpmPriority = returnValue;
            InitSpmPriorities2();
        }));
    }

    private void InitSpmPriorities2() {
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().enableColumnResizing();

        HostStatusColumn<VDS> statusIconColumn = new HostStatusColumn<>();
        statusIconColumn.setContextMenuTitle(constants.statusIconHost());
        getTable().addColumn(statusIconColumn, constants.empty(), "35px"); //$NON-NLS-1$

        HostAdditionalStatusColumn additionalStatusColumn = new HostAdditionalStatusColumn();
        additionalStatusColumn.setContextMenuTitle(constants.additionalStatusHost());
        getTable().addColumn(additionalStatusColumn, constants.empty(), "60px"); //$NON-NLS-1$

        AbstractTextColumn<VDS> nameColumn = new AbstractLinkColumn<VDS>(new FieldUpdater<VDS, String>() {

                @Override
                public void update(int index, VDS host, String value) {
                    Map<String, String> parameters = new HashMap<>();
                    parameters.put(FragmentParams.NAME.getName(), host.getName());
                    //The link was clicked, now fire an event to switch to details.
                    getPlaceTransitionHandler().handlePlaceTransition(
                            WebAdminApplicationPlaces.hostGeneralSubTabPlace, parameters);
                }

            }) {

            @Override
            public String getValue(VDS object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable(VdsConditionFieldAutoCompleter.NAME);
        getTable().addColumn(nameColumn, constants.nameHost(), "150px"); //$NON-NLS-1$

        CommentColumn<VDS> commentColumn = new CommentColumn<>();
        getTable().addColumnWithHtmlHeader(commentColumn,
                SafeHtmlUtils.fromSafeConstant(constants.commentLabel()),
                "75px"); //$NON-NLS-1$

        AbstractTextColumn<VDS> hostColumn = new AbstractTextColumn<VDS>() {
            @Override
            public String getValue(VDS object) {
                return object.getHostName();
            }
        };
        hostColumn.makeSortable(VdsConditionFieldAutoCompleter.ADDRESS);
        getTable().addColumn(hostColumn, constants.ipHost(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<VDS> clusterColumn = new AbstractLinkColumn<VDS>(new FieldUpdater<VDS, String>() {
            @Override
            public void update(int index, VDS host, String value) {
                Map<String, String> parameters = new HashMap<>();
                parameters.put(FragmentParams.NAME.getName(), host.getClusterName());
                getPlaceTransitionHandler().handlePlaceTransition(
                        WebAdminApplicationPlaces.clusterGeneralSubTabPlace, parameters);
            }
        }) {
            @Override
            public String getValue(VDS object) {
                return object.getClusterName();
            }
        };

        clusterColumn.makeSortable(VdsConditionFieldAutoCompleter.CLUSTER);
        getTable().addColumn(clusterColumn, constants.clusterHost(), "150px"); //$NON-NLS-1$

        if (ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly) {
            AbstractTextColumn<VDS> dcColumn = new AbstractLinkColumn<VDS>(new FieldUpdater<VDS, String>() {
                @Override
                public void update(int index, VDS host, String value) {
                    Map<String, String> parameters = new HashMap<>();
                    parameters.put(FragmentParams.NAME.getName(), host.getStoragePoolName());
                    getPlaceTransitionHandler().handlePlaceTransition(
                            WebAdminApplicationPlaces.dataCenterStorageSubTabPlace, parameters);
                }
            }) {
                @Override
                public String getValue(VDS object) {
                    return object.getStoragePoolName();
                }
            };
            dcColumn.makeSortable(VdsConditionFieldAutoCompleter.DATACENTER);
            getTable().addColumn(dcColumn, constants.dcHost(), "150px"); //$NON-NLS-1$
        }

        AbstractTextColumn<VDS> statusColumn = new AbstractTextColumn<VDS>() {
            @Override
            public String getValue(VDS object) {
                return object.getStatus() + networkState(object);
            }

            private String networkState(VDS vds) {
                return vds.isNetworkOperationInProgress() ? NETWORKS_UPDATING : "";
            }
        };

        ReasonColumn<VDS> reasonColumn = new ReasonColumn<VDS>() {

            @Override
            protected String getReason(VDS value) {
                return value.getMaintenanceReason();
            }

        };

        List<HasCell<VDS, ?>> list = new ArrayList<>();
        list.add(statusColumn);
        list.add(reasonColumn);

        Cell<VDS> compositeCell = new StatusCompositeCell<>(list);

        AbstractColumn<VDS, VDS> statusTextColumn = new AbstractColumn<VDS, VDS>(compositeCell) {
            @Override
            public VDS getValue(VDS object) {
                return object;
            }

            @Override
            public SafeHtml getTooltip(VDS value) {
                String maintenanceReason = value.getMaintenanceReason();
                if (maintenanceReason != null && !maintenanceReason.trim().isEmpty()) {
                    return SafeHtmlUtils.fromString(maintenanceReason);
                }
                return null;
            }
        };

        statusTextColumn.makeSortable(VdsConditionFieldAutoCompleter.STATUS);
        getTable().addColumn(statusTextColumn, constants.statusHost(), "220px"); //$NON-NLS-1$

        if (ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly) {
            VmCountColumn vmCountColumn = new VmCountColumn();
            vmCountColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
            vmCountColumn.makeSortable(VdsConditionFieldAutoCompleter.ACTIVE_VMS);
            getTable().addColumn(vmCountColumn, constants.vmsCount(), "110px"); //$NON-NLS-1$
        }

        AbstractPercentColumn<VDS> memColumn = new AbstractPercentColumn<VDS>() {
            @Override
            public Integer getProgressValue(VDS object) {
                return object.getUsageMemPercent();
            }
        };
        memColumn.makeSortable(VdsConditionFieldAutoCompleter.MEM_USAGE);
        getTable().addColumn(memColumn, constants.memoryHost(), "80px"); //$NON-NLS-1$

        AbstractPercentColumn<VDS> cpuColumn = new AbstractPercentColumn<VDS>() {
            @Override
            public Integer getProgressValue(VDS object) {
                return object.getUsageCpuPercent();
            }
        };
        cpuColumn.makeSortable(VdsConditionFieldAutoCompleter.CPU_USAGE);
        getTable().addColumn(cpuColumn, constants.cpuHost(), "80px"); //$NON-NLS-1$

        AbstractPercentColumn<VDS> netColumn = new AbstractPercentColumn<VDS>() {
            @Override
            public Integer getProgressValue(VDS object) {
                return object.getUsageNetworkPercent();
            }
        };
        netColumn.makeSortable(VdsConditionFieldAutoCompleter.NETWORK_USAGE);
        getTable().addColumn(netColumn, constants.networkHost(), "80px"); //$NON-NLS-1$

        if (ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly) {
            AbstractTextColumn<VDS> spmColumn = new AbstractTextColumn<VDS>() {
                @Override
                public String getValue(VDS object) {
                    int value = object.getVdsSpmPriority();
                    int lowValue = defaultSpmPriority / 2;
                    int highValue = defaultSpmPriority + (maxSpmPriority - defaultSpmPriority) / 2;

                    if (object.getSpmStatus() != VdsSpmStatus.None){
                        return object.getSpmStatus().name();
                    }

                    if (value == -1) {
                        return constants.spmNeverText();
                    } else if (value == lowValue) {
                        return constants.spmLowText();
                    } else if (value == defaultSpmPriority) {
                        return constants.spmNormalText();
                    } else if (value == highValue) {
                        return constants.spmHighText();
                    }

                    return constants.spmCustomText();
                }
            };
            getTable().addColumn(spmColumn, constants.spmPriorityHost(), "100px"); //$NON-NLS-1$
        }
    }

    @Override
    public SimpleActionTable<Void, VDS> getTable() {
        return super.getTable();
    }

}
