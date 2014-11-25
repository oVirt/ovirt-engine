package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkHostListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.NetworkSelectionChangeEvent;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabNetworkHostPresenter extends AbstractSubTabPresenter<NetworkView, NetworkListModel, NetworkHostListModel, SubTabNetworkHostPresenter.ViewDef, SubTabNetworkHostPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.networkHostSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabNetworkHostPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<NetworkView> {
    }

    @TabInfo(container = NetworkSubTabPanelPresenter.class)
    static TabData getTabData(ApplicationConstants applicationConstants,
            SearchableDetailModelProvider<PairQueryable<VdsNetworkInterface, VDS>, NetworkListModel, NetworkHostListModel> modelProvider) {
        return new ModelBoundTabData(applicationConstants.networkHostSubTabLabel(), 4, modelProvider);
    }

    @Inject
    public SubTabNetworkHostPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager,
            SearchableDetailModelProvider<PairQueryable<VdsNetworkInterface, VDS>, NetworkListModel, NetworkHostListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider,
                NetworkSubTabPanelPresenter.TYPE_SetTabContent);
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.networkMainTabPlace);
    }

    @ProxyEvent
    public void onNetworkSelectionChange(NetworkSelectionChangeEvent event) {
        updateMainTabSelection(event.getSelectedItems());
    }

}

