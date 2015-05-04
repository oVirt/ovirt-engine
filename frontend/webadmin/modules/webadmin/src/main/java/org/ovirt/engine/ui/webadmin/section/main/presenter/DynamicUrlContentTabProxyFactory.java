package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.common.auth.LoggedInGatekeeper;
import org.ovirt.engine.ui.common.widget.Align;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.ChangeTabHandler;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

public class DynamicUrlContentTabProxyFactory {

    private final PlaceManager placeManager;
    private final EventBus eventBus;
    private final LoggedInGatekeeper gatekeeper;
    private final Provider<DynamicUrlContentTabPresenter.ViewDef> viewProvider;

    @Inject
    public DynamicUrlContentTabProxyFactory(PlaceManager placeManager,
            EventBus eventBus,
            LoggedInGatekeeper gatekeeper,
            Provider<DynamicUrlContentTabPresenter.ViewDef> viewProvider) {
        this.placeManager = placeManager;
        this.eventBus = eventBus;
        this.gatekeeper = gatekeeper;
        this.viewProvider = viewProvider;
    }

    public DynamicUrlContentTabProxy create(
            Type<RequestTabsHandler> requestTabsEventType,
            Type<ChangeTabHandler> changeTabEventType,
            Type<RevealContentHandler<?>> slot,
            String label, float priority, String historyToken,
            boolean isMainTab, String contentUrl, Align align) {
        return new DynamicUrlContentTabProxy(
                placeManager, eventBus, gatekeeper,
                requestTabsEventType, changeTabEventType,
                slot, viewProvider,
                label, priority, historyToken,
                isMainTab, contentUrl, align);
    }

}
