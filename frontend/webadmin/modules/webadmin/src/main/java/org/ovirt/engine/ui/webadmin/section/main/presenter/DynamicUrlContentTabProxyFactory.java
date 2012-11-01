package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;

import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

public class DynamicUrlContentTabProxyFactory {

    private final ClientGinjector ginjector;
    private final Provider<DynamicUrlContentTabPresenter.ViewDef> viewProvider;

    @Inject
    public DynamicUrlContentTabProxyFactory(ClientGinjector ginjector,
            Provider<DynamicUrlContentTabPresenter.ViewDef> viewProvider) {
        this.ginjector = ginjector;
        this.viewProvider = viewProvider;
    }

    public DynamicUrlContentTabProxy create(
            Type<RequestTabsHandler> requestTabsEventType,
            Type<RevealContentHandler<?>> revealContentEventType,
            String label, String historyToken,
            boolean isMainTab, String contentUrl) {
        return new DynamicUrlContentTabProxy(ginjector,
                requestTabsEventType, revealContentEventType, viewProvider,
                label, Float.MAX_VALUE, historyToken, isMainTab, contentUrl);
    }

}
