package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.common.auth.LoggedInGatekeeper;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

public class DynamicUrlContentProxyFactory {
    private final PlaceManager placeManager;
    private final EventBus eventBus;
    private final LoggedInGatekeeper gatekeeper;
    private final Provider<DynamicUrlContentPresenter.ViewDef> viewProvider;

    @Inject
    public DynamicUrlContentProxyFactory(PlaceManager placeManager,
            EventBus eventBus,
            LoggedInGatekeeper gatekeeper,
            Provider<DynamicUrlContentPresenter.ViewDef> viewProvider) {
        this.placeManager = placeManager;
        this.eventBus = eventBus;
        this.gatekeeper = gatekeeper;
        this.viewProvider = viewProvider;
    }

    public DynamicUrlContentProxy create(
            String historyToken, String contentUrl) {
        return new DynamicUrlContentProxy(historyToken, contentUrl, gatekeeper, placeManager, eventBus, viewProvider,
                MainContentPresenter.TYPE_SetContent);
    }

}
