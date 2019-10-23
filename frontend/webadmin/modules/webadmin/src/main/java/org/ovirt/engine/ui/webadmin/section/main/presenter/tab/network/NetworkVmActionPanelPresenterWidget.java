package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkVmListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class NetworkVmActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<NetworkView, PairQueryable<VmNetworkInterface, VM>, NetworkListModel, NetworkVmListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public NetworkVmActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<NetworkView, PairQueryable<VmNetworkInterface, VM>> view,
            SearchableDetailModelProvider<PairQueryable<VmNetworkInterface, VM>, NetworkListModel,
                NetworkVmListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<NetworkView, PairQueryable<VmNetworkInterface, VM>>(constants.removeInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
