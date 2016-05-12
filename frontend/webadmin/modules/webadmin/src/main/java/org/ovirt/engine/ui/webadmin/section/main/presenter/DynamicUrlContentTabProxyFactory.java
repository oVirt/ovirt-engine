package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.common.auth.LoggedInGatekeeper;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.uicommonweb.models.CommonModel;

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
    private final Provider<CommonModel> commonModelProvider;

    @Inject
    public DynamicUrlContentTabProxyFactory(PlaceManager placeManager,
            EventBus eventBus,
            LoggedInGatekeeper gatekeeper,
            Provider<DynamicUrlContentTabPresenter.ViewDef> viewProvider,
            Provider<CommonModel> commonModelProvider) {
        this.placeManager = placeManager;
        this.eventBus = eventBus;
        this.gatekeeper = gatekeeper;
        this.viewProvider = viewProvider;
        this.commonModelProvider = commonModelProvider;
    }

    public DynamicUrlContentTabProxy create(
            Type<RequestTabsHandler> requestTabsEventType,
            Type<ChangeTabHandler> changeTabEventType,
            Type<RevealContentHandler<?>> slot,
            String label, float priority, String historyToken,
            boolean isMainTab, String contentUrl, Align align,
            String searchPrefix) {
        if (isMainTab) {
            commonModelProvider.get().addPluginModel(historyToken,
                    searchPrefix != null ? searchPrefix : label.replace(":", "")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return new DynamicUrlContentTabProxy(
                placeManager, eventBus, gatekeeper,
                requestTabsEventType, changeTabEventType,
                slot, viewProvider,
                label, priority, historyToken,
                isMainTab, contentUrl, align, commonModelProvider);
    }

}
