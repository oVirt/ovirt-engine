package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterNetworkListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class ClusterNetworkActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<Cluster, Network, ClusterListModel<Void>, ClusterNetworkListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public ClusterNetworkActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<Cluster, Network> view,
            SearchableDetailModelProvider<Network, ClusterListModel<Void>, ClusterNetworkListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<Cluster, Network>(constants.addNetworkNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getNewNetworkCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<Cluster, Network>(constants.assignDetatchNetworksNework()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getManageCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<Cluster, Network>(constants.setAsDisplayNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getSetAsDisplayCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<Cluster, Network>(constants.syncAllClusterNetworks()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getSyncAllNetworksCommand();
            }
        });
    }
}
