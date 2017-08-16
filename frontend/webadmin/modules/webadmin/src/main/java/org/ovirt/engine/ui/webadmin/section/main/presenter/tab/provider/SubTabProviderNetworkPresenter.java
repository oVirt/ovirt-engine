package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.provider;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderListModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderNetworkListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DetailTabDataIndex;

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

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.providerNetworkSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabProviderNetworkPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<Provider> {
        void setNetworkClickHandler(FieldUpdater<NetworkView, String> fieldUpdater);
    }

    @TabInfo(container = ProviderSubTabPanelPresenter.class)
    static TabData getTabData() {
        return DetailTabDataIndex.PROVIDER_NETWORK;
    }

    @Inject
    public SubTabProviderNetworkPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, ProviderMainSelectedItems selectedItems,
            ProviderNetworkActionPanelPresenterWidget actionPanel,
            SearchableDetailModelProvider<NetworkView, ProviderListModel, ProviderNetworkListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, actionPanel,
                ProviderSubTabPanelPresenter.TYPE_SetTabContent);
    }

}
