package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network;

import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkExternalSubnetListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabNetworkExternalSubnetPresenter
    extends AbstractSubTabNetworkPresenter<NetworkExternalSubnetListModel, SubTabNetworkExternalSubnetPresenter.ViewDef,
        SubTabNetworkExternalSubnetPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.networkExternalSubnetSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabNetworkExternalSubnetPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<NetworkView> {
    }

    @TabInfo(container = NetworkSubTabPanelPresenter.class)
    static TabData getTabData(
            SearchableDetailModelProvider<ExternalSubnet, NetworkListModel, NetworkExternalSubnetListModel> modelProvider) {
        return new ModelBoundTabData(constants.networkExternalSubnetSubTabLabel(), 2,
                modelProvider);
    }

    @Inject
    public SubTabNetworkExternalSubnetPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, NetworkMainTabSelectedItems selectedItems,
            SearchableDetailModelProvider<ExternalSubnet, NetworkListModel, NetworkExternalSubnetListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems,
                NetworkSubTabPanelPresenter.TYPE_SetTabContent);
    }
}
