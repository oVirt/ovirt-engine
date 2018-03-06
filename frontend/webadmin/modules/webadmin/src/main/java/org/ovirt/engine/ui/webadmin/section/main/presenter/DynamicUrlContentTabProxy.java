package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.common.presenter.DynamicTabProxy;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.ChangeTabHandler;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.proxy.Gatekeeper;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

public class DynamicUrlContentTabProxy extends DynamicTabProxy<DynamicUrlContentTabPresenter> {

    private final EventBus eventBus;
    private final Type<RevealContentHandler<?>> slot;
    private final Provider<DynamicUrlContentTabPresenter.ViewDef> viewProvider;

    private final String contentUrl;

    public DynamicUrlContentTabProxy(PlaceManager placeManager,
            EventBus eventBus, Gatekeeper gatekeeper,
            Type<RequestTabsHandler> requestTabsEventType,
            Type<ChangeTabHandler> changeTabEventType,
            Type<RevealContentHandler<?>> slot,
            Provider<DynamicUrlContentTabPresenter.ViewDef> viewProvider,
            String label, float priority, String historyToken,
            String contentUrl) {
        super(placeManager, eventBus, gatekeeper,
                requestTabsEventType, changeTabEventType,
                label, priority, historyToken);
        this.eventBus = eventBus;
        this.slot = slot;
        this.viewProvider = viewProvider;
        this.contentUrl = contentUrl;
    }

    @Override
    protected DynamicUrlContentTabPresenter createPresenter() {
        return new DynamicUrlContentTabPresenter(
                eventBus, viewProvider.get(), this,
                getPlaceManager(), slot, contentUrl);
    }

}
