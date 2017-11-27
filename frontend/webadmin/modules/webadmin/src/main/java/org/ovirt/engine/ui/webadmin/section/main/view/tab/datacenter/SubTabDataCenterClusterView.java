package org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.FragmentParams;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLinkColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterClusterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;

public class SubTabDataCenterClusterView extends AbstractSubTabTableView<StoragePool, Cluster, DataCenterListModel, DataCenterClusterListModel>
        implements SubTabDataCenterClusterPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabDataCenterClusterView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabDataCenterClusterView(SearchableDetailModelProvider<Cluster, DataCenterListModel, DataCenterClusterListModel> modelProvider) {
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

        AbstractTextColumn<Cluster> nameColumn = new AbstractLinkColumn<Cluster>(new FieldUpdater<Cluster, String>() {
            @Override
            public void update(int index, Cluster cluster, String value) {
                Map<String, String> parameters = new HashMap<>();
                parameters.put(FragmentParams.NAME.getName(), cluster.getName());
                getPlaceTransitionHandler().handlePlaceTransition(
                        WebAdminApplicationPlaces.clusterGeneralSubTabPlace, parameters);
            }
        }) {
            @Override
            public String getValue(Cluster object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameCluster(), "300px"); //$NON-NLS-1$

        AbstractTextColumn<Cluster> versionColumn = new AbstractTextColumn<Cluster>() {
            @Override
            public String getValue(Cluster object) {
                return object.getCompatibilityVersion().getValue();
            }
        };
        versionColumn.makeSortable();
        getTable().addColumn(versionColumn, constants.comptVersCluster(), "300px"); //$NON-NLS-1$

        AbstractTextColumn<Cluster> descColumn = new AbstractTextColumn<Cluster>() {
            @Override
            public String getValue(Cluster object) {
                return object.getDescription();
            }
        };
        descColumn.makeSortable();
        getTable().addColumn(descColumn, constants.descriptionCluster());
    }

}
