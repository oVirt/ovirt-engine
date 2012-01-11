package org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterNetworkListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterNetworkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.gwt.core.client.GWT;

public class SubTabDataCenterNetworkView extends AbstractSubTabTableView<storage_pool, network, DataCenterListModel, DataCenterNetworkListModel>
        implements SubTabDataCenterNetworkPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabDataCenterNetworkView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabDataCenterNetworkView(SearchableDetailModelProvider<network, DataCenterListModel, DataCenterNetworkListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        TextColumnWithTooltip<network> nameColumn = new TextColumnWithTooltip<network>() {
            @Override
            public String getValue(network object) {
                return object.getname();
            }
        };
        getTable().addColumn(nameColumn, "Name");

        TextColumnWithTooltip<network> typeColumn = new TextColumnWithTooltip<network>() {
            @Override
            public String getValue(network object) {
                return object.getdescription();
            }
        };
        getTable().addColumn(typeColumn, "Description");

        getTable().addActionButton(new WebAdminButtonDefinition<network>("New") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getNewCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<network>("Edit") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getEditCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<network>("Remove") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
