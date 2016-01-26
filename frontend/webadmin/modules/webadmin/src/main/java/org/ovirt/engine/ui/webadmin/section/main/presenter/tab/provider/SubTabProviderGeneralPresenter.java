package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.provider;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderListModel;
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

public class SubTabProviderGeneralPresenter
    extends AbstractSubTabProviderPresenter<ProviderGeneralModel, SubTabProviderGeneralPresenter.ViewDef,
        SubTabProviderGeneralPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.providerGeneralSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabProviderGeneralPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<Provider> {
    }

    @TabInfo(container = ProviderSubTabPanelPresenter.class)
    static TabData getTabData(
            DetailModelProvider<ProviderListModel, ProviderGeneralModel> modelProvider) {
        return new ModelBoundTabData(constants.providerGeneralSubTabLabel(), 0, modelProvider);
    }

    @Inject
    public SubTabProviderGeneralPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, ProviderMainTabSelectedItems selectedItems,
            DetailModelProvider<ProviderListModel, ProviderGeneralModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems,
                ProviderSubTabPanelPresenter.TYPE_SetTabContent);
    }
}
