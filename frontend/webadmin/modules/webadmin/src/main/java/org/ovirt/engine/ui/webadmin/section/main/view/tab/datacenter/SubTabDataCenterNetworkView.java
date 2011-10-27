package org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterNetworkListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterNetworkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.UiCommandButtonDefinition;

import com.google.gwt.user.cellview.client.TextColumn;

public class SubTabDataCenterNetworkView extends AbstractSubTabTableView<storage_pool, network, DataCenterListModel, DataCenterNetworkListModel>
        implements SubTabDataCenterNetworkPresenter.ViewDef {

    @Inject
    public SubTabDataCenterNetworkView(SearchableDetailModelProvider<network, DataCenterListModel, DataCenterNetworkListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        TextColumn<network> nameColumn = new TextColumn<network>() {
            @Override
            public String getValue(network object) {
                return object.getname();
            }
        };
        getTable().addColumn(nameColumn, "Name");

        TextColumn<network> typeColumn = new TextColumn<network>() {
            @Override
            public String getValue(network object) {
                return object.getdescription();
            }
        };
        getTable().addColumn(typeColumn, "Description");

        getTable().addActionButton(new UiCommandButtonDefinition<network>(getDetailModel().getNewCommand(), "New"));
        getTable().addActionButton(new UiCommandButtonDefinition<network>(getDetailModel().getEditCommand(), "Edit"));
        getTable().addActionButton(new UiCommandButtonDefinition<network>(getDetailModel().getRemoveCommand(), "Remove"));

    }

}
