package org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterClusterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;

import com.google.gwt.user.cellview.client.TextColumn;

public class SubTabDataCenterClusterView extends AbstractSubTabTableView<storage_pool, VDSGroup, DataCenterListModel, DataCenterClusterListModel>
        implements SubTabDataCenterClusterPresenter.ViewDef {

    @Inject
    public SubTabDataCenterClusterView(SearchableDetailModelProvider<VDSGroup, DataCenterListModel, DataCenterClusterListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        TextColumn<VDSGroup> nameColumn = new TextColumn<VDSGroup>() {
            @Override
            public String getValue(VDSGroup object) {
                return object.getname();
            }
        };
        getTable().addColumn(nameColumn, "Name");

        TextColumn<VDSGroup> versionColumn = new TextColumn<VDSGroup>() {
            @Override
            public String getValue(VDSGroup object) {
                return object.getcompatibility_version().getValue();
            }
        };
        getTable().addColumn(versionColumn, "Compatiblity Version");

        TextColumn<VDSGroup> descColumn = new TextColumn<VDSGroup>() {
            @Override
            public String getValue(VDSGroup object) {
                return object.getdescription();
            }
        };
        getTable().addColumn(descColumn, "Description");
    }

}
