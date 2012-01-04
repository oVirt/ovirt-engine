package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterNetworkListModel;
import org.ovirt.engine.ui.webadmin.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterNetworkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.NetworkStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.TextColumnWithTooltip;

import com.google.gwt.core.client.GWT;

public class SubTabClusterNetworkView extends AbstractSubTabTableView<VDSGroup, network, ClusterListModel, ClusterNetworkListModel>
        implements SubTabClusterNetworkPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabClusterNetworkView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabClusterNetworkView(SearchableDetailModelProvider<network, ClusterListModel, ClusterNetworkListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().addColumn(new NetworkStatusColumn(), "", "20px");

        TextColumnWithTooltip<network> nameColumn = new TextColumnWithTooltip<network>() {
            @Override
            public String getValue(network object) {
                return object.getname();
            }
        };
        getTable().addColumn(nameColumn, "Name");

        TextColumnWithTooltip<network> statusColumn = new EnumColumn<network, NetworkStatus>() {
            @Override
            public NetworkStatus getRawValue(network object) {
                return object.getStatus();
            }
        };
        getTable().addColumn(statusColumn, "Status");

        TextColumnWithTooltip<network> roleColumn = new TextColumnWithTooltip<network>() {
            @Override
            public String getValue(network object) {
                // according to ClusterNetworkListView.xaml:45
                return object.getis_display() ? "Display" : "";
            }
        };
        getTable().addColumn(roleColumn, "Role");

        TextColumnWithTooltip<network> descColumn = new TextColumnWithTooltip<network>() {
            @Override
            public String getValue(network object) {
                return object.getdescription();
            }
        };
        getTable().addColumn(descColumn, "Description");

        getTable().addActionButton(new UiCommandButtonDefinition<network>("Add Network") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getNewNetworkCommand();
            }
        });

        getTable().addActionButton(new UiCommandButtonDefinition<network>("Assign/Detach Networks") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getManageCommand();
            }
        });

        getTable().addActionButton(new UiCommandButtonDefinition<network>("Set as Display") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getSetAsDisplayCommand();
            }
        });
    }

}
