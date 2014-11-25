package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkVmListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
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

public class SubTabNetworkVmPresenter extends AbstractSubTabPresenter<NetworkView, NetworkListModel, NetworkVmListModel, SubTabNetworkVmPresenter.ViewDef, SubTabNetworkVmPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.networkVmSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabNetworkVmPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<NetworkView> {
    }

    @TabInfo(container = NetworkSubTabPanelPresenter.class)
    static TabData getTabData(ApplicationConstants applicationConstants,
            SearchableDetailModelProvider<PairQueryable<VmNetworkInterface, VM>, NetworkListModel, NetworkVmListModel> modelProvider) {
        return new ModelBoundTabData(applicationConstants.networkVmSubTabLabel(), 5, modelProvider);
    }

    @Inject
    public SubTabNetworkVmPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, SearchableDetailModelProvider<PairQueryable<VmNetworkInterface, VM>, NetworkListModel, NetworkVmListModel> modelProvider) {
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

