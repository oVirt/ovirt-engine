package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.webadmin.section.main.presenter.SetDynamicTabContentUrlEvent.SetDynamicTabContentUrlHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.SetDynamicTabUnloadHandlerEvent.SetDynamicTabUnloadHandlerHandler;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

public class DynamicUrlContentPresenter extends Presenter<DynamicUrlContentPresenter.ViewDef, DynamicUrlContentProxy>
    implements SetDynamicTabContentUrlHandler, SetDynamicTabUnloadHandlerHandler {

    public interface ViewDef extends View {
        void setContentUrl(String url);
        void setUnloadHandler(JavaScriptObject unloadHandler);
    }

    public DynamicUrlContentPresenter(EventBus eventBus, ViewDef view,
            DynamicUrlContentProxy proxy, Type<RevealContentHandler<?>> slot,
            String contentUrl) {
        super(eventBus, view, proxy, slot);
        setContentUrl(contentUrl);
    }

    @Override
    protected void onBind() {
        super.onBind();
        registerHandler(getEventBus().addHandler(SetDynamicTabContentUrlEvent.getType(), this));
        registerHandler(getEventBus().addHandler(SetDynamicTabUnloadHandlerEvent.getType(), this));
    }

    @Override
    public void onSetDynamicTabContentUrl(SetDynamicTabContentUrlEvent event) {
        if (getProxy().getNameToken().equals(event.getHistoryToken())) {
            setContentUrl(event.getContentUrl());
        }
    }

    public void setContentUrl(String contentUrl) {
        getView().setContentUrl(contentUrl);
    }

    @Override
    public void onSetDynamicTabUnloadHandler(SetDynamicTabUnloadHandlerEvent event) {
        if (getProxy().getNameToken().equals(event.getHistoryToken())) {
            getView().setUnloadHandler(event.getUnloadHandler());
        }
    }

}
