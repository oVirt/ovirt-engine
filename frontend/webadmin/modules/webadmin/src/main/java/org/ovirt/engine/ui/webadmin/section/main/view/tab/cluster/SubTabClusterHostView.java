package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.FragmentParams;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLinkColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterHostListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterHostPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.table.column.HostStatusColumn;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;

public class SubTabClusterHostView extends AbstractSubTabTableView<Cluster, VDS, ClusterListModel<Void>, ClusterHostListModel>
        implements SubTabClusterHostPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabClusterHostView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final String NETWORKS_UPDATING = " - " + constants.networksUpdating() //$NON-NLS-1$
            + "..."; //$NON-NLS-1$

    @Inject
    public SubTabClusterHostView(SearchableDetailModelProvider<VDS, ClusterListModel<Void>, ClusterHostListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTableContainer());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable() {
        getTable().enableColumnResizing();

        HostStatusColumn statusIconColumn = new HostStatusColumn();
        statusIconColumn.setContextMenuTitle(constants.statusIconClusterHost());
        getTable().addColumn(statusIconColumn, constants.empty(), "35px"); //$NON-NLS-1$

        AbstractTextColumn<VDS> nameColumn = new AbstractLinkColumn<VDS>(new FieldUpdater<VDS, String>() {
            @Override
            public void update(int index, VDS host, String value) {
                Map<String, String> parameters = new HashMap<>();
                parameters.put(FragmentParams.NAME.getName(), host.getName());
                getPlaceTransitionHandler().handlePlaceTransition(
                        WebAdminApplicationPlaces.hostGeneralSubTabPlace, parameters);
            }
        }) {
            @Override
            public String getValue(VDS object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameClusterHost(), "220px"); //$NON-NLS-1$

        AbstractTextColumn<VDS> hostColumn = new AbstractTextColumn<VDS>() {
            @Override
            public String getValue(VDS object) {
                return object.getHostName();
            }
        };
        hostColumn.makeSortable();
        getTable().addColumn(hostColumn, constants.hostIpClusterHost(), "220px"); //$NON-NLS-1$

        AbstractTextColumn<VDS> statusColumn = new AbstractTextColumn<VDS>() {
            @Override
            public String getValue(VDS object) {
                return object.getStatus() + networkState(object);
            }
            private String networkState(VDS vds) {
                return vds.isNetworkOperationInProgress() ? NETWORKS_UPDATING : "";
            }
        };
        statusColumn.makeSortable();
        getTable().addColumn(statusColumn, constants.statusClusterHost(), "220px"); //$NON-NLS-1$

        if (ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly)) {
            AbstractTextColumn<VDS> loadColumn = new AbstractTextColumn<VDS>() {
                @Override
                public String getValue(VDS object) {
                    int numOfActiveVMs = object.getVmActive() != null ? object.getVmActive() : 0;
                    return ConstantsManager.getInstance().getMessages().numberOfVmsForHostsLoad(numOfActiveVMs);
                }
            };
            loadColumn.makeSortable();
            getTable().addColumn(loadColumn, constants.loadClusterHost(), "120px"); //$NON-NLS-1$
        }

        if (ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly)) {
            AbstractTextColumn<VDS> consoleColumn = new AbstractTextColumn<VDS>() {
                @Override
                public String getValue(VDS host) {
                    return host.getConsoleAddress() != null ? constants.yes() : constants.no();
                }

            };
            consoleColumn.makeSortable();
            getTable().addColumn(consoleColumn, constants.overriddenConsoleAddress(), "220px"); //$NON-NLS-1$
        }
    }
}
