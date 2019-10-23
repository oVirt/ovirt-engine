package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkExternalSubnetListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class NetworkExternalSubnetActionPanelPresenterWidget<M extends ListWithDetailsModel,
    D extends NetworkExternalSubnetListModel> extends DetailActionPanelPresenterWidget<NetworkView, ExternalSubnet, M, D> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public NetworkExternalSubnetActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<NetworkView, ExternalSubnet> view,
            SearchableDetailModelProvider<ExternalSubnet, NetworkListModel, NetworkExternalSubnetListModel>
                dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<NetworkView, ExternalSubnet>(constants.newNetworkExternalSubnet()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getNewCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<NetworkView, ExternalSubnet>(constants.removeNetworkExternalSubnet()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
