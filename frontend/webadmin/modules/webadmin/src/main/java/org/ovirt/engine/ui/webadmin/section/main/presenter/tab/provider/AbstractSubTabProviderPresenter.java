package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.provider;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;

import com.google.gwt.event.shared.EventBus;
import com.gwtplatform.mvp.client.presenter.slots.NestedSlot;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public abstract class AbstractSubTabProviderPresenter<D extends HasEntity<?>,
    V extends AbstractSubTabPresenter.ViewDef<Provider>, P extends TabContentProxyPlace<?>>
        extends AbstractSubTabPresenter<Provider, ProviderListModel, D, V, P> {

    public AbstractSubTabProviderPresenter(EventBus eventBus, V view, P proxy, PlaceManager placeManager,
            DetailModelProvider<ProviderListModel, D> modelProvider, ProviderMainSelectedItems selectedItems,
            DetailActionPanelPresenterWidget<?, ?, ProviderListModel, ?> actionPanel,
            NestedSlot slot) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, actionPanel, slot);
    }

    @Override
    protected PlaceRequest getMainContentRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.providerMainPlace);
    }
}
