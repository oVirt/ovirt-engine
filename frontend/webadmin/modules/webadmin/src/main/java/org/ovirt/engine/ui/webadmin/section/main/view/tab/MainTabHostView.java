package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.searchbackend.VdsConditionFieldAutoCompleter;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.action.CommandLocation;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.common.widget.table.cell.Cell;
import org.ovirt.engine.ui.common.widget.table.cell.StatusCompositeCell;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.ReportInit;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabHostPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.uicommon.ReportActionsHelper;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminMenuBarButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.AbstractPercentColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.CommentColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.HostAdditionalStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.HostStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.ReasonColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmCountColumn;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.inject.Inject;

public class MainTabHostView extends AbstractMainTabWithDetailsTableView<VDS, HostListModel<Void>> implements MainTabHostPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabHostView> {

        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    int maxSpmPriority;
    int defaultSpmPriority;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public MainTabHostView(MainModelProvider<VDS, HostListModel<Void>> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);

        InitSpmPriorities();
    }

    private void InitSpmPriorities() {
        AsyncDataProvider.getInstance().getMaxSpmPriority(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {

                MainTabHostView view = (MainTabHostView) target;

                view.maxSpmPriority = (Integer) returnValue;
                InitSpmPriorities1();
            }
        }));
    }

    private void InitSpmPriorities1() {
        AsyncDataProvider.getInstance().getDefaultSpmPriority(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {

                MainTabHostView view = (MainTabHostView) target;

                view.defaultSpmPriority = (Integer) returnValue;
                InitSpmPriorities2();
            }
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
        getTable().addColumn(statusIconColumn, constants.empty(), "30px"); //$NON-NLS-1$

        HostAdditionalStatusColumn additionalStatusColumn = new HostAdditionalStatusColumn();
        additionalStatusColumn.setContextMenuTitle(constants.additionalStatusHost());
        getTable().addColumn(additionalStatusColumn, constants.empty(), "60px"); //$NON-NLS-1$

        AbstractTextColumn<VDS> nameColumn = new AbstractTextColumn<VDS>() {
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

        AbstractTextColumn<VDS> clusterColumn = new AbstractTextColumn<VDS>() {
            @Override
            public String getValue(VDS object) {
                return object.getClusterName();
            }
        };
        clusterColumn.makeSortable(VdsConditionFieldAutoCompleter.CLUSTER);
        getTable().addColumn(clusterColumn, constants.clusterHost(), "150px"); //$NON-NLS-1$

        if (ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly) {
            AbstractTextColumn<VDS> dcColumn = new AbstractTextColumn<VDS>() {
                @Override
                public String getValue(VDS object) {
                    return object.getStoragePoolName();
                }
            };
            dcColumn.makeSortable(VdsConditionFieldAutoCompleter.DATACENTER);
            getTable().addColumn(dcColumn, constants.dcHost(), "150px"); //$NON-NLS-1$
        }

        AbstractTextColumn<VDS> statusColumn = new AbstractEnumColumn<VDS, VDSStatus>() {
            @Override
            public VDSStatus getRawValue(VDS object) {
                return object.getStatus();
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
        getTable().addColumn(statusTextColumn, constants.statusHost(), "100px"); //$NON-NLS-1$

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
        getTable().addColumn(memColumn, constants.memoryHost(), "60px"); //$NON-NLS-1$

        AbstractPercentColumn<VDS> cpuColumn = new AbstractPercentColumn<VDS>() {
            @Override
            public Integer getProgressValue(VDS object) {
                return object.getUsageCpuPercent();
            }
        };
        cpuColumn.makeSortable(VdsConditionFieldAutoCompleter.CPU_USAGE);
        getTable().addColumn(cpuColumn, constants.cpuHost(), "60px"); //$NON-NLS-1$

        AbstractPercentColumn<VDS> netColumn = new AbstractPercentColumn<VDS>() {
            @Override
            public Integer getProgressValue(VDS object) {
                return object.getUsageNetworkPercent();
            }
        };
        netColumn.makeSortable(VdsConditionFieldAutoCompleter.NETWORK_USAGE);
        getTable().addColumn(netColumn, constants.networkHost(), "60px"); //$NON-NLS-1$

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

        getTable().addActionButton(new WebAdminButtonDefinition<VDS>(constants.newHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getNewCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<VDS>(constants.editHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getEditCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<VDS>(constants.removeHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRemoveCommand();
            }
        });
        // TODO: separator
        getTable().addActionButton(new WebAdminButtonDefinition<VDS>(constants.activateHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getActivateCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<VDS>(constants.maintenanceHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getMaintenanceCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<VDS>(constants.selectHostAsSPM()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getSelectAsSpmCommand();
            }
        });
        if (ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly) {
            getTable().addActionButton(new WebAdminButtonDefinition<VDS>(constants.numaSupport()) {
                @Override
                protected UICommand resolveCommand() {
                    return getMainModel().getNumaSupportCommand();
                }
            });
            getTable().addActionButton(new WebAdminButtonDefinition<VDS>(constants.confirmRebootedHost(),
                    CommandLocation.OnlyFromContext) {
                @Override
                protected UICommand resolveCommand() {
                    return getMainModel().getManualFenceCommand();
                }
            });
        }
        // TODO: separator
        getTable().addActionButton(new WebAdminButtonDefinition<VDS>(constants.approveHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getApproveCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<VDS>(constants.reinstallHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getInstallCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<VDS>(constants.upgradeHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getUpgradeCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<VDS>(constants.configureLocalStorageHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getConfigureLocalStorageCommand();
            }
        });

        List<ActionButtonDefinition<VDS>> pmSubActions = new LinkedList<>();

        pmSubActions.add(new WebAdminButtonDefinition<VDS>(constants.restartHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRestartCommand();
            }
        });

        pmSubActions.add(new WebAdminButtonDefinition<VDS>(constants.startHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getStartCommand();
            }
        });

        pmSubActions.add(new WebAdminButtonDefinition<VDS>(constants.stopHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getStopCommand();
            }
        });

        if (ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly) {
            getTable().addActionButton(new WebAdminMenuBarButtonDefinition<>(constants.pmHost(),
                    pmSubActions,
                    CommandLocation.OnlyFromToolBar));
        }

        getTable().addActionButton(new WebAdminButtonDefinition<VDS>(constants.assignTagsHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getAssignTagsCommand();
            }
        });

        if (ReportInit.getInstance().isReportsEnabled()) {
            updateReportsAvailability();
        } else {
            getMainModel().getReportsAvailabilityEvent().addListener(new IEventListener<EventArgs>() {
                @Override
                public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                    updateReportsAvailability();
                }
            });
        }

        getTable().addActionButton(new WebAdminButtonDefinition<VDS>(constants.refreshHostCapabilities()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRefreshCapabilitiesCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<VDS>(constants.enrollCertificate()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getEnrollCertificateCommand();
            }
        });
    }

    private void updateReportsAvailability() {
        if (ReportInit.getInstance().isReportsEnabled()) {
            List<ActionButtonDefinition<VDS>> resourceSubActions =
                    ReportActionsHelper.getInstance().getResourceSubActions("Host", getModelProvider()); //$NON-NLS-1$
            if (resourceSubActions != null && resourceSubActions.size() > 0) {
                getTable().addActionButton(new WebAdminMenuBarButtonDefinition<>(constants.showReportHost(), resourceSubActions));
            }
        }
    }

    @Override
    public SimpleActionTable<VDS> getTable() {
        return super.getTable();
    }

}
