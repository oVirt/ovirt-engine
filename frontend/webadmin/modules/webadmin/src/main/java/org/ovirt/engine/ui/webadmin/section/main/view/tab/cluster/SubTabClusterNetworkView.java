package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterNetworkListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterNetworkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.NetworkStatusColumn;

import com.google.gwt.core.client.GWT;

public class SubTabClusterNetworkView extends AbstractSubTabTableView<VDSGroup, Network, ClusterListModel, ClusterNetworkListModel>
        implements SubTabClusterNetworkPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabClusterNetworkView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabClusterNetworkView(SearchableDetailModelProvider<Network, ClusterListModel, ClusterNetworkListModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(constants);
        initWidget(getTable());
    }

    void initTable(final ApplicationConstants constants) {
        getTable().addColumn(new NetworkStatusColumn(), "", "20px"); //$NON-NLS-1$ //$NON-NLS-2$

        TextColumnWithTooltip<Network> nameColumn = new TextColumnWithTooltip<Network>() {
            @Override
            public String getValue(Network object) {
                return object.getname();
            }
        };
        getTable().addColumn(nameColumn, constants.nameNetwork());

        TextColumnWithTooltip<Network> statusColumn = new EnumColumn<Network, NetworkStatus>() {
            @Override
            public NetworkStatus getRawValue(Network object) {
                return object.getCluster().getstatus();
            }
        };
        getTable().addColumn(statusColumn, constants.statusNetwork());

        TextColumnWithTooltip<Network> roleColumn = new TextColumnWithTooltip<Network>() {
            @Override
            public String getValue(Network object) {
                // according to ClusterNetworkListView.xaml:45
                return (object.getCluster() == null ? false : object.getCluster().getis_display()) ? constants.displayNetwork() : constants.empty();
            }
        };
        getTable().addColumn(roleColumn, constants.roleNetwork());

        TextColumnWithTooltip<Network> descColumn = new TextColumnWithTooltip<Network>() {
            @Override
            public String getValue(Network object) {
                return object.getDescription();
            }
        };
        getTable().addColumn(descColumn, constants.descriptionNetwork());

        getTable().addActionButton(new WebAdminButtonDefinition<Network>(constants.addNetworkNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getNewNetworkCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<Network>(constants.assignDetatchNetworksNework()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getManageCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<Network>(constants.setAsDisplayNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getSetAsDisplayCommand();
            }
        });
    }

}
