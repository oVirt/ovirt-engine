package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.searchbackend.ClusterConditionFieldAutoCompleter;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.ReportInit;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabClusterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.uicommon.ReportActionsHelper;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminImageButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminMenuBarButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.CommentColumn;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class MainTabClusterView extends AbstractMainTabWithDetailsTableView<VDSGroup, ClusterListModel> implements
    MainTabClusterPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabClusterView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public MainTabClusterView(MainModelProvider<VDSGroup, ClusterListModel> modelProvider,
            ApplicationResources resources, ApplicationConstants constants) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(resources, constants);
        initWidget(getTable());
    }

    void initTable(final ApplicationResources resources, final ApplicationConstants constants) {
        getTable().enableColumnResizing();

        TextColumnWithTooltip<VDSGroup> nameColumn = new TextColumnWithTooltip<VDSGroup>() {
            @Override
            public String getValue(VDSGroup object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable(ClusterConditionFieldAutoCompleter.NAME);
        getTable().addColumn(nameColumn, constants.nameCluster(), "150px"); //$NON-NLS-1$

        CommentColumn<VDSGroup> commentColumn = new CommentColumn<VDSGroup>();
        getTable().addColumnWithHtmlHeader(commentColumn, commentColumn.getHeaderHtml(), "30px"); //$NON-NLS-1$

        if (ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly) {
            TextColumnWithTooltip<VDSGroup> dataCenterColumn = new TextColumnWithTooltip<VDSGroup>() {
                @Override
                public String getValue(VDSGroup object) {
                    return object.getStoragePoolName();
                }
            };
            getTable().addColumn(dataCenterColumn, constants.dcCluster(), "150px"); //$NON-NLS-1$
        }

        TextColumnWithTooltip<VDSGroup> versionColumn = new TextColumnWithTooltip<VDSGroup>() {
            @Override
            public String getValue(VDSGroup object) {
                return object.getcompatibility_version().getValue();
            }
        };
        getTable().addColumn(versionColumn, constants.comptVersCluster(), "150px"); //$NON-NLS-1$

        TextColumnWithTooltip<VDSGroup> descColumn = new TextColumnWithTooltip<VDSGroup>() {
            @Override
            public String getValue(VDSGroup object) {
                return object.getdescription();
            }
        };
        descColumn.makeSortable(ClusterConditionFieldAutoCompleter.DESCRIPTION);
        getTable().addColumn(descColumn, constants.descriptionCluster(), "300px"); //$NON-NLS-1$

        if (ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly) {
            TextColumnWithTooltip<VDSGroup> cpuTypeColumn = new TextColumnWithTooltip<VDSGroup>() {
                @Override
                public String getValue(VDSGroup object) {
                    return object.getcpu_name();
                }
            };
            getTable().addColumn(cpuTypeColumn, constants.cpuTypeCluster(), "150px"); //$NON-NLS-1$
        }

        TextColumnWithTooltip<VDSGroup> hostCountColumn = new TextColumnWithTooltip<VDSGroup>() {
            @Override
            public String getValue(VDSGroup object) {
                if (object.getGroupHostsAndVms() == null) {
                    return "";
                }
                return object.getGroupHostsAndVms().getHosts() + "";
            }
        };

        getTable().addColumn(hostCountColumn, constants.hostCount(), "150px"); //$NON-NLS-1$

        TextColumnWithTooltip<VDSGroup> vmCountColumn = new TextColumnWithTooltip<VDSGroup>() {
            @Override
            public String getValue(VDSGroup object) {
                if (object.getGroupHostsAndVms() == null) {
                    return "";
                }
                return object.getGroupHostsAndVms().getVms() + "";
            }
        };

        getTable().addColumn(vmCountColumn, constants.vmCount(), "150px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<VDSGroup>(constants.newCluster()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getNewCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<VDSGroup>(constants.editCluster()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getEditCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<VDSGroup>(constants.removeCluster()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRemoveCommand();
            }
        });

        if (ReportInit.getInstance().isReportsEnabled()) {
            updateReportsAvailability(constants);
        } else {
            getMainModel().getReportsAvailabilityEvent().addListener(new IEventListener<EventArgs>() {
                @Override
                public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                    updateReportsAvailability(constants);
                }
            });
        }

        getTable().addActionButton(new WebAdminImageButtonDefinition<VDSGroup>(constants.guideMeCluster(),
                resources.guideSmallImage(), resources.guideSmallDisabledImage(), true) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getGuideCommand();
            }
        });
    }

    public void updateReportsAvailability(ApplicationConstants constants) {

        if (ReportInit.getInstance().isReportsEnabled()) {
            List<ActionButtonDefinition<VDSGroup>> resourceSubActions =
                    ReportActionsHelper.getInstance().getResourceSubActions("Cluster", getModelProvider()); //$NON-NLS-1$
            if (resourceSubActions != null && resourceSubActions.size() > 0) {
                getTable().addActionButton(new WebAdminMenuBarButtonDefinition<VDSGroup>(constants.showReportCluster(),
                        resourceSubActions));
            }
        }

    }
}
