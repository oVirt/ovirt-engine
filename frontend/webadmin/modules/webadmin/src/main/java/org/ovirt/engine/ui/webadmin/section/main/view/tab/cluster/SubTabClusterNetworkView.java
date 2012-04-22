package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network;
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

public class SubTabClusterNetworkView extends AbstractSubTabTableView<VDSGroup, network, ClusterListModel, ClusterNetworkListModel>
        implements SubTabClusterNetworkPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabClusterNetworkView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabClusterNetworkView(SearchableDetailModelProvider<network, ClusterListModel, ClusterNetworkListModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(constants);
        initWidget(getTable());
    }

    void initTable(final ApplicationConstants constants) {
        getTable().addColumn(new NetworkStatusColumn(), constants.empty(), "20px"); //$NON-NLS-1$

        TextColumnWithTooltip<network> nameColumn = new TextColumnWithTooltip<network>() {
            @Override
            public String getValue(network object) {
                return object.getname();
            }
        };
        getTable().addColumn(nameColumn, constants.nameNetwork());

        TextColumnWithTooltip<network> statusColumn = new EnumColumn<network, NetworkStatus>() {
            @Override
            public NetworkStatus getRawValue(network object) {
                return object.getStatus();
            }
        };
        getTable().addColumn(statusColumn, constants.statusNetwork());

        TextColumnWithTooltip<network> roleColumn = new TextColumnWithTooltip<network>() {
            @Override
            public String getValue(network object) {
                // according to ClusterNetworkListView.xaml:45
                return object.getis_display() ? constants.displayNetwork() : constants.empty();
            }
        };
        getTable().addColumn(roleColumn, constants.roleNetwork());

        TextColumnWithTooltip<network> descColumn = new TextColumnWithTooltip<network>() {
            @Override
            public String getValue(network object) {
                return object.getdescription();
            }
        };
        getTable().addColumn(descColumn, constants.descriptionNetwork());

        getTable().addActionButton(new WebAdminButtonDefinition<network>(constants.addNetworkNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getNewNetworkCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<network>(constants.assignDetatchNetworksNework()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getManageCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<network>(constants.setAsDisplayNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getSetAsDisplayCommand();
            }
        });
    }

}
