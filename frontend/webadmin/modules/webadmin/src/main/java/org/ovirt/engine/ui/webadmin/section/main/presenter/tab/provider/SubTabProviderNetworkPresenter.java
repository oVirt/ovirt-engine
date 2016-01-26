package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.provider;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderListModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderNetworkListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SystemTreeModelProvider;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabProviderNetworkPresenter
    extends AbstractSubTabProviderPresenter<ProviderNetworkListModel, SubTabProviderNetworkPresenter.ViewDef,
        SubTabProviderNetworkPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private SystemTreeModelProvider systemTreeModelProvider;

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.providerNetworkSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabProviderNetworkPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<Provider> {
        void setNetworkClickHandler(FieldUpdater<NetworkView, String> fieldUpdater);
    }

    @TabInfo(container = ProviderSubTabPanelPresenter.class)
    static TabData getTabData(
            SearchableDetailModelProvider<NetworkView, ProviderListModel, ProviderNetworkListModel> modelProvider) {
        return new ModelBoundTabData(constants.providerNetworksSubTabLabel(), 1, modelProvider);
    }

    @Inject
    public SubTabProviderNetworkPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, ProviderMainTabSelectedItems selectedItems,
            SearchableDetailModelProvider<NetworkView, ProviderListModel, ProviderNetworkListModel> modelProvider,
                SystemTreeModelProvider systemTreeModelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems,
                ProviderSubTabPanelPresenter.TYPE_SetTabContent);
        this.systemTreeModelProvider = systemTreeModelProvider;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setNetworkClickHandler(new FieldUpdater<NetworkView, String>() {

            @Override
            public void update(int index, NetworkView network, String value) {
                systemTreeModelProvider.setSelectedItem(network.getId());
            }
        });
    }

}
