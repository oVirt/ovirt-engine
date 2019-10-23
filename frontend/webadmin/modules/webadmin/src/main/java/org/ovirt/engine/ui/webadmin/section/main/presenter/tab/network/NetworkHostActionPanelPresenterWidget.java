package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkHostListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class NetworkHostActionPanelPresenterWidget
    extends DetailActionPanelPresenterWidget<NetworkView, PairQueryable<VdsNetworkInterface, VDS>,
        NetworkListModel, NetworkHostListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public NetworkHostActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<NetworkView, PairQueryable<VdsNetworkInterface, VDS>> view,
            SearchableDetailModelProvider<PairQueryable<VdsNetworkInterface, VDS>, NetworkListModel,
                NetworkHostListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<NetworkView, PairQueryable<VdsNetworkInterface,
                VDS>>(constants.setupHostNetworksInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getSetupNetworksCommand();
            }
        });
    }
}
