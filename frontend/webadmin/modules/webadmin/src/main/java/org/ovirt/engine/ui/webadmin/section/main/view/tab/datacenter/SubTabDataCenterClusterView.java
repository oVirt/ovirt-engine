package org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterClusterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

public class SubTabDataCenterClusterView extends AbstractSubTabTableView<storage_pool, VDSGroup, DataCenterListModel, DataCenterClusterListModel>
        implements SubTabDataCenterClusterPresenter.ViewDef {

    @Inject
    public SubTabDataCenterClusterView(SearchableDetailModelProvider<VDSGroup, DataCenterListModel, DataCenterClusterListModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        initTable(constants);
        initWidget(getTable());
    }

    void initTable(ApplicationConstants constants) {
        TextColumnWithTooltip<VDSGroup> nameColumn = new TextColumnWithTooltip<VDSGroup>() {
            @Override
            public String getValue(VDSGroup object) {
                return object.getname();
            }
        };
        getTable().addColumn(nameColumn, constants.nameCluster());

        TextColumnWithTooltip<VDSGroup> versionColumn = new TextColumnWithTooltip<VDSGroup>() {
            @Override
            public String getValue(VDSGroup object) {
                return object.getcompatibility_version().getValue();
            }
        };
        getTable().addColumn(versionColumn, constants.comptVersCluster());

        TextColumnWithTooltip<VDSGroup> descColumn = new TextColumnWithTooltip<VDSGroup>() {
            @Override
            public String getValue(VDSGroup object) {
                return object.getdescription();
            }
        };
        getTable().addColumn(descColumn, constants.descriptionCluster());
    }

}
