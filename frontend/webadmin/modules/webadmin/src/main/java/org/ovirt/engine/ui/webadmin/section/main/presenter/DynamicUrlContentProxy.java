package org.ovirt.engine.ui.webadmin.section.main.presenter;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.common.client.StandardProvider;
import com.gwtplatform.mvp.client.proxy.Gatekeeper;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceWithGatekeeper;
import com.gwtplatform.mvp.client.proxy.ProxyImpl;
import com.gwtplatform.mvp.client.proxy.ProxyPlaceImpl;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

public class DynamicUrlContentProxy extends ProxyPlaceImpl<DynamicUrlContentPresenter>
    implements Provider<DynamicUrlContentPresenter> {

    public static class WrappedProxy extends ProxyImpl<DynamicUrlContentPresenter> {
        public WrappedProxy(PlaceManager placeManager,
                EventBus eventBus, Provider<DynamicUrlContentPresenter> presenterProvider) {
            this.bind(placeManager, eventBus);
            this.presenter = new StandardProvider<>(presenterProvider);
        }
    }

    private DynamicUrlContentPresenter presenter;
    private final Type<RevealContentHandler<?>> slot;
    private final String contentUrl;
    private final Provider<DynamicUrlContentPresenter.ViewDef> viewProvider;

    public DynamicUrlContentProxy(String historyToken, String contentUrl, Gatekeeper gatekeeper,
            PlaceManager placeManager, EventBus eventBus, Provider<DynamicUrlContentPresenter.ViewDef> viewProvider,
            Type<RevealContentHandler<?>> slot) {
        bind(placeManager, eventBus);
        this.slot = slot;
        this.viewProvider = viewProvider;
        this.contentUrl = contentUrl;
        setProxy(new WrappedProxy(placeManager, eventBus, this));
        setPlace(new PlaceWithGatekeeper(historyToken, gatekeeper));
        // Create and bind presenter eagerly (don't wait for reveal request)
        Scheduler.get().scheduleDeferred(() -> get());
    }

    @Override
    public void manualRevealFailed() {
        super.manualRevealFailed();
        getPlaceManager().revealDefaultPlace();
    }

    @Override
    public DynamicUrlContentPresenter get() {
        if (presenter == null) {
            presenter = new DynamicUrlContentPresenter(getEventBus(), viewProvider.get(), this, slot, contentUrl);
            presenter.bind();
        }
        return presenter;
    }
}
