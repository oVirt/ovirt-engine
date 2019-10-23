package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.provider;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderListModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderNetworkListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class ProviderNetworkActionPanelPresenterWidget
    extends DetailActionPanelPresenterWidget<Provider, NetworkView, ProviderListModel, ProviderNetworkListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public ProviderNetworkActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<Provider, NetworkView> view,
            SearchableDetailModelProvider<NetworkView, ProviderListModel, ProviderNetworkListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<Provider, NetworkView>(constants.importNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getDiscoverCommand();
            }
        });
    }

}
