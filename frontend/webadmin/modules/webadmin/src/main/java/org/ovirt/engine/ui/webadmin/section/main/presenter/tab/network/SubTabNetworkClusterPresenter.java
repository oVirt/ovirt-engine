package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network;

import org.ovirt.engine.core.common.businessentities.NetworkView;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.NetworkCluster;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.place.ApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.NetworkSelectionChangeEvent;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabNetworkClusterPresenter extends AbstractSubTabPresenter<NetworkView, NetworkListModel, NetworkClusterListModel, SubTabNetworkClusterPresenter.ViewDef, SubTabNetworkClusterPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(ApplicationPlaces.networkClusterSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabNetworkClusterPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<NetworkView> {
    }

    @TabInfo(container = NetworkSubTabPanelPresenter.class)
    static TabData getTabData(ClientGinjector ginjector) {
        return new ModelBoundTabData(ginjector.getApplicationConstants().networkClusterSubTabLabel(), 1,
                ginjector.getSubTabNetworkClusterModelProvider());
    }

    @Inject
    public SubTabNetworkClusterPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager,
            SearchableDetailModelProvider<PairQueryable<VDSGroup, NetworkCluster>, NetworkListModel, NetworkClusterListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider);
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, NetworkSubTabPanelPresenter.TYPE_SetTabContent, this);
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return new PlaceRequest(ApplicationPlaces.networkMainTabPlace);
    }

    @ProxyEvent
    public void onNetworkSelectionChange(NetworkSelectionChangeEvent event) {
        updateMainTabSelection(event.getSelectedItems());
    }

}

