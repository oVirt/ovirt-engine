package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.common.presenter.DynamicTabProxy;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;

import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

public class DynamicUrlContentTabProxy extends DynamicTabProxy<DynamicUrlContentTabPresenter> {

    private final Type<RevealContentHandler<?>> revealContentEventType;
    private final Provider<DynamicUrlContentTabPresenter.ViewDef> viewProvider;
    private final boolean isMainTab;
    private final String contentUrl;

    public DynamicUrlContentTabProxy(ClientGinjector ginjector,
            Type<RequestTabsHandler> requestTabsEventType,
            Type<RevealContentHandler<?>> revealContentEventType,
            Provider<DynamicUrlContentTabPresenter.ViewDef> viewProvider,
            String label, float priority, String historyToken,
            boolean isMainTab, String contentUrl, Align align) {
        super(ginjector, requestTabsEventType, label, priority, historyToken, align);
        this.revealContentEventType = revealContentEventType;
        this.viewProvider = viewProvider;
        this.isMainTab = isMainTab;
        this.contentUrl = contentUrl;
    }

    @Override
    protected DynamicUrlContentTabPresenter createPresenter() {
        return new DynamicUrlContentTabPresenter(getEventBus(), viewProvider.get(),
                this, placeManager, revealContentEventType, isMainTab, contentUrl);
    }

}
