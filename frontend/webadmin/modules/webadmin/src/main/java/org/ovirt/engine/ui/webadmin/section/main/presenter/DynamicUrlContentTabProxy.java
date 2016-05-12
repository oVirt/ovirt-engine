package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.common.presenter.DynamicTabProxy;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.uicommonweb.models.CommonModel;

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

    private final boolean isMainTab;
    private final String contentUrl;
    private final Provider<CommonModel> commonModelProvider;

    public DynamicUrlContentTabProxy(PlaceManager placeManager,
            EventBus eventBus, Gatekeeper gatekeeper,
            Type<RequestTabsHandler> requestTabsEventType,
            Type<ChangeTabHandler> changeTabEventType,
            Type<RevealContentHandler<?>> slot,
            Provider<DynamicUrlContentTabPresenter.ViewDef> viewProvider,
            String label, float priority, String historyToken,
            boolean isMainTab, String contentUrl, Align align,
            Provider<CommonModel> commonModelProvider) {
        super(placeManager, eventBus, gatekeeper,
                requestTabsEventType, changeTabEventType,
                label, priority, historyToken, align);
        this.eventBus = eventBus;
        this.slot = slot;
        this.viewProvider = viewProvider;
        this.isMainTab = isMainTab;
        this.contentUrl = contentUrl;
        this.commonModelProvider = commonModelProvider;
    }

    @Override
    protected DynamicUrlContentTabPresenter createPresenter() {
        return new DynamicUrlContentTabPresenter(
                eventBus, viewProvider.get(), this,
                getPlaceManager(), slot, isMainTab, contentUrl, commonModelProvider);
    }

}
