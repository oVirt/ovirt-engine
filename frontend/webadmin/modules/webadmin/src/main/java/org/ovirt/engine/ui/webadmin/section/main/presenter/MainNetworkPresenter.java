package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.NetworkBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.NetworkActionPanelPresenterWidget;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class MainNetworkPresenter extends AbstractMainWithDetailsPresenter<NetworkView, NetworkListModel, MainNetworkPresenter.ViewDef, MainNetworkPresenter.ProxyDef> {

    @GenEvent
    public class NetworkSelectionChange {

        List<NetworkView> selectedItems;

    }

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.networkMainPlace)
    public interface ProxyDef extends ProxyPlace<MainNetworkPresenter> {
    }

    public interface ViewDef extends AbstractMainWithDetailsPresenter.ViewDef<NetworkView> {
        void setProviderClickHandler(FieldUpdater<NetworkView, String> fieldUpdater);
    }

    @Inject
    public MainNetworkPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, MainModelProvider<NetworkView, NetworkListModel> modelProvider,
            SearchPanelPresenterWidget<NetworkView, NetworkListModel> searchPanelPresenterWidget,
            NetworkBreadCrumbsPresenterWidget breadCrumbs,
            NetworkActionPanelPresenterWidget actionPanel) {
        super(eventBus, view, proxy, placeManager, modelProvider, searchPanelPresenterWidget, breadCrumbs, actionPanel);
    }

    @Override
    protected void fireTableSelectionChangeEvent() {
        NetworkSelectionChangeEvent.fire(this, getSelectedItems());
    }

    @Override
    protected PlaceRequest getMainViewRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.networkMainPlace);
    }

}
