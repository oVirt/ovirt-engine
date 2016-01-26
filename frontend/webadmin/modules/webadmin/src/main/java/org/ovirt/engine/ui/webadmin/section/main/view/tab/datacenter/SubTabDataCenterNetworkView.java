package org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterNetworkListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterNetworkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import com.google.gwt.core.client.GWT;

public class SubTabDataCenterNetworkView extends AbstractSubTabTableView<StoragePool, Network, DataCenterListModel, DataCenterNetworkListModel>
        implements SubTabDataCenterNetworkPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabDataCenterNetworkView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabDataCenterNetworkView(SearchableDetailModelProvider<Network, DataCenterListModel, DataCenterNetworkListModel> modelProvider) {
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

        AbstractTextColumn<Network> nameColumn = new AbstractTextColumn<Network>() {
            @Override
            public String getValue(Network object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameNetwork(), "400px"); //$NON-NLS-1$

        AbstractTextColumn<Network> descriptionColumn = new AbstractTextColumn<Network>() {
            @Override
            public String getValue(Network object) {
                return object.getDescription();
            }
        };
        descriptionColumn.makeSortable();
        getTable().addColumn(descriptionColumn, constants.descriptionNetwork(), "400px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<Network>(constants.newNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getNewCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<Network>(constants.editNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getEditCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<Network>(constants.removeNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
