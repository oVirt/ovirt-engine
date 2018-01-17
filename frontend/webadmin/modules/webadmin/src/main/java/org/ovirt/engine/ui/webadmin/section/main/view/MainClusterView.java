package org.ovirt.engine.ui.webadmin.section.main.view;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.searchbackend.ClusterConditionFieldAutoCompleter;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.FragmentParams;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLinkColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainClusterPresenter;
import org.ovirt.engine.ui.webadmin.widget.table.column.ClusterAdditionalStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.CommentColumn;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.inject.Inject;

public class MainClusterView extends AbstractMainWithDetailsTableView<Cluster, ClusterListModel<Void>> implements
    MainClusterPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainClusterView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public MainClusterView(MainModelProvider<Cluster, ClusterListModel<Void>> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().enableColumnResizing();

        AbstractTextColumn<Cluster> nameColumn = new AbstractLinkColumn<Cluster>(new FieldUpdater<Cluster, String>() {

            @Override
            public void update(int index, Cluster cluster, String value) {
                Map<String, String> parameters = new HashMap<>();
                parameters.put(FragmentParams.NAME.getName(), cluster.getName());
                //The link was clicked, now fire an event to switch to details.
                getPlaceTransitionHandler().handlePlaceTransition(
                        WebAdminApplicationPlaces.clusterGeneralSubTabPlace, parameters);
            }

        }) {
            @Override
            public String getValue(Cluster object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable(ClusterConditionFieldAutoCompleter.NAME);
        getTable().addColumn(nameColumn, constants.nameCluster(), "150px"); //$NON-NLS-1$

        ClusterAdditionalStatusColumn additionalStatusColumn = new ClusterAdditionalStatusColumn();
        additionalStatusColumn.setContextMenuTitle(constants.additionalStatusCluster());
        getTable().addColumn(additionalStatusColumn, constants.empty(), "30px"); //$NON-NLS-1$

        CommentColumn<Cluster> commentColumn = new CommentColumn<>();
        getTable().addColumnWithHtmlHeader(commentColumn,
                SafeHtmlUtils.fromSafeConstant(constants.commentLabel()),
                "75px"); //$NON-NLS-1$

        if (ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly) {
            AbstractTextColumn<Cluster> dataCenterColumn = new AbstractTextColumn<Cluster>() {
                @Override
                public String getValue(Cluster object) {
                    return object.getStoragePoolName();
                }
            };
            getTable().addColumn(dataCenterColumn, constants.dcCluster(), "150px"); //$NON-NLS-1$
        }

        AbstractTextColumn<Cluster> versionColumn = new AbstractTextColumn<Cluster>() {
            @Override
            public String getValue(Cluster object) {
                return object.getCompatibilityVersion().getValue();
            }
        };
        getTable().addColumn(versionColumn, constants.comptVersCluster(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<Cluster> descColumn = new AbstractTextColumn<Cluster>() {
            @Override
            public String getValue(Cluster object) {
                return object.getDescription();
            }
        };
        descColumn.makeSortable(ClusterConditionFieldAutoCompleter.DESCRIPTION);
        getTable().addColumn(descColumn, constants.descriptionCluster(), "300px"); //$NON-NLS-1$

        if (ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly) {
            AbstractTextColumn<Cluster> cpuTypeColumn = new AbstractTextColumn<Cluster>() {
                @Override
                public String getValue(Cluster object) {
                    return object.getCpuName();
                }
            };
            getTable().addColumn(cpuTypeColumn, constants.cpuTypeCluster(), "150px"); //$NON-NLS-1$
        }

        AbstractTextColumn<Cluster> hostCountColumn = new AbstractTextColumn<Cluster>() {
            @Override
            public String getValue(Cluster object) {
                if (object.getClusterHostsAndVms() == null) {
                    return ""; //$NON-NLS-1$
                }
                return object.getClusterHostsAndVms().getHosts() + ""; //$NON-NLS-1$
            }
        };

        getTable().addColumn(hostCountColumn, constants.hostCount(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<Cluster> vmCountColumn = new AbstractTextColumn<Cluster>() {
            @Override
            public String getValue(Cluster object) {
                if (object.getClusterHostsAndVms() == null) {
                    return ""; //$NON-NLS-1$
                }
                return object.getClusterHostsAndVms().getVms() + ""; //$NON-NLS-1$
            }
        };

        getTable().addColumn(vmCountColumn, constants.vmCount(), "150px"); //$NON-NLS-1$
    }
}
