package org.ovirt.engine.ui.webadmin.section.main.presenter;

import javax.inject.Inject;

import org.ovirt.engine.ui.webadmin.section.main.presenter.SetDynamicTabContentUrlEvent.SetDynamicTabContentUrlHandler;

import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

public class DynamicUrlContentPresenter extends Presenter<DynamicUrlContentPresenter.ViewDef, DynamicUrlContentProxy>
    implements SetDynamicTabContentUrlHandler {

    public interface ViewDef extends View {
        void setContentUrl(String url);
    }

    @Inject
    public DynamicUrlContentPresenter(String contentUrl, EventBus eventBus, ViewDef view, DynamicUrlContentProxy proxy,
            Type<RevealContentHandler<?>> slot) {
        super(eventBus, view, proxy, slot);
        setContentUrl(contentUrl);
    }

    @Override
    protected void onBind() {
        super.onBind();
        registerHandler(getEventBus().addHandler(SetDynamicTabContentUrlEvent.getType(), this));
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
}
