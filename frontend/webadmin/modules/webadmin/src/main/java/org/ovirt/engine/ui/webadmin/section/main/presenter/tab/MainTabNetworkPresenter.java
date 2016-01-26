package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractMainTabWithDetailsPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.uicommon.model.SystemTreeModelProvider;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class MainTabNetworkPresenter extends AbstractMainTabWithDetailsPresenter<NetworkView, NetworkListModel, MainTabNetworkPresenter.ViewDef, MainTabNetworkPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private SystemTreeModelProvider systemTreeModelProvider;

    @GenEvent
    public class NetworkSelectionChange {

        List<NetworkView> selectedItems;

    }

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.networkMainTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<MainTabNetworkPresenter> {
    }

    public interface ViewDef extends AbstractMainTabWithDetailsPresenter.ViewDef<NetworkView> {
        void setProviderClickHandler(FieldUpdater<NetworkView, String> fieldUpdater);
    }

    @TabInfo(container = MainTabPanelPresenter.class)
    static TabData getTabData(
            MainModelProvider<NetworkView, NetworkListModel> modelProvider) {
        return new ModelBoundTabData(constants.networkMainTabLabel(), 3, modelProvider);
    }

    @Inject
    public MainTabNetworkPresenter(EventBus eventBus,
            ViewDef view,
            ProxyDef proxy,
            PlaceManager placeManager,
            MainModelProvider<NetworkView, NetworkListModel> modelProvider,
            SystemTreeModelProvider systemTreeModelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider);
        this.systemTreeModelProvider = systemTreeModelProvider;
    }

    @Override
    protected void fireTableSelectionChangeEvent() {
        NetworkSelectionChangeEvent.fire(this, getSelectedItems());
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.networkMainTabPlace);
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setProviderClickHandler(new FieldUpdater<NetworkView, String> () {
            @Override
            public void update(int index, NetworkView network, String value) {
                systemTreeModelProvider.setSelectedItem(network.getProvidedBy().getProviderId());
            }
        });
    }

}
