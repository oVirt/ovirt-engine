package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.searchbackend.ClusterConditionFieldAutoCompleter;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
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
import org.ovirt.engine.ui.webadmin.widget.table.column.CommentColumn2;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class MainTabClusterView extends AbstractMainTabWithDetailsTableView<VDSGroup, ClusterListModel<Void>> implements
    MainTabClusterPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabClusterView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public MainTabClusterView(MainModelProvider<VDSGroup, ClusterListModel<Void>> modelProvider,
            ApplicationResources resources, ApplicationConstants constants) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(resources, constants);
        initWidget(getTable());
    }

    void initTable(final ApplicationResources resources, final ApplicationConstants constants) {
        getTable().enableColumnResizing();

        AbstractTextColumn<VDSGroup> nameColumn = new AbstractTextColumn<VDSGroup>() {
            @Override
            public String getValue(VDSGroup object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable(ClusterConditionFieldAutoCompleter.NAME);
        getTable().addColumn(nameColumn, constants.nameCluster(), "150px"); //$NON-NLS-1$

        CommentColumn2<VDSGroup> commentColumn = new CommentColumn2<VDSGroup>();
        // TODO: add support for tooltips on headers
        // TODO: don't hardcode "Comment" -- use image
        // getTable().addColumnWithHtmlHeader(commentColumn, commentColumn.getHeaderHtml(), "30px"); //$NON-NLS-1$
        getTable().addColumn(commentColumn, constants.commentLabel(), "50px"); //$NON-NLS-1$ //$NON-NLS-2$

        if (ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly) {
            AbstractTextColumn<VDSGroup> dataCenterColumn = new AbstractTextColumn<VDSGroup>() {
                @Override
                public String getValue(VDSGroup object) {
                    return object.getStoragePoolName();
                }
            };
            getTable().addColumn(dataCenterColumn, constants.dcCluster(), "150px"); //$NON-NLS-1$
        }

        AbstractTextColumn<VDSGroup> versionColumn = new AbstractTextColumn<VDSGroup>() {
            @Override
            public String getValue(VDSGroup object) {
                return object.getCompatibilityVersion().getValue();
            }
        };
        getTable().addColumn(versionColumn, constants.comptVersCluster(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<VDSGroup> descColumn = new AbstractTextColumn<VDSGroup>() {
            @Override
            public String getValue(VDSGroup object) {
                return object.getDescription();
            }
        };
        descColumn.makeSortable(ClusterConditionFieldAutoCompleter.DESCRIPTION);
        getTable().addColumn(descColumn, constants.descriptionCluster(), "300px"); //$NON-NLS-1$

        if (ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly) {
            AbstractTextColumn<VDSGroup> cpuTypeColumn = new AbstractTextColumn<VDSGroup>() {
                @Override
                public String getValue(VDSGroup object) {
                    return object.getCpuName();
                }
            };
            getTable().addColumn(cpuTypeColumn, constants.cpuTypeCluster(), "150px"); //$NON-NLS-1$
        }

        AbstractTextColumn<VDSGroup> hostCountColumn = new AbstractTextColumn<VDSGroup>() {
            @Override
            public String getValue(VDSGroup object) {
                if (object.getGroupHostsAndVms() == null) {
                    return "";
                }
                return object.getGroupHostsAndVms().getHosts() + "";
            }
        };

        getTable().addColumn(hostCountColumn, constants.hostCount(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<VDSGroup> vmCountColumn = new AbstractTextColumn<VDSGroup>() {
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
