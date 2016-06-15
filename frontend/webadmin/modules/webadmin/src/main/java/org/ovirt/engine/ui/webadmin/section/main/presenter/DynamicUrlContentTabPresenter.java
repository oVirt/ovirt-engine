package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.SetDynamicTabContentUrlEvent.SetDynamicTabContentUrlHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

public class DynamicUrlContentTabPresenter extends WebAdminDynamicTabPresenter<DynamicUrlContentTabPresenter.ViewDef, DynamicUrlContentTabProxy> implements SetDynamicTabContentUrlHandler {

    public interface ViewDef extends View {

        void setContentUrl(String url);

    }

    private final boolean isMainTab;
    private final Provider<CommonModel> commonModelProvider;

    public DynamicUrlContentTabPresenter(EventBus eventBus, ViewDef view,
            DynamicUrlContentTabProxy proxy, PlaceManager placeManager,
            Type<RevealContentHandler<?>> slot,
            boolean isMainTab, String contentUrl,
            Provider<CommonModel> commonModelProvider) {
        super(eventBus, view, proxy, placeManager, slot);
        this.isMainTab = isMainTab;
        this.commonModelProvider = commonModelProvider;
        setContentUrl(contentUrl);
    }

    @Override
    protected void onBind() {
        super.onBind();
        registerHandler(getEventBus().addHandler(SetDynamicTabContentUrlEvent.getType(), this));
    }

    @Override
    public void onSetDynamicTabContentUrl(SetDynamicTabContentUrlEvent event) {
        if (getProxy().getTargetHistoryToken().equals(event.getHistoryToken())) {
            setContentUrl(event.getContentUrl());
        }
    }

    @Override
    protected void onReveal() {
        super.onReveal();

        if (isMainTab) {
            commonModelProvider.get().setPluginTabSelected(getProxy().getTargetHistoryToken());
        }
    }

    @Override
    protected boolean isMainTab() {
        return isMainTab;
    }

    public void setContentUrl(String contentUrl) {
        getView().setContentUrl(contentUrl);
    }

}
