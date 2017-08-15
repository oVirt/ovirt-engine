package org.ovirt.engine.ui.webadmin.section.main.presenter;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.Proxy;

public abstract class AbstractOverlayPresenter<V extends View, P extends Proxy<?>> extends Presenter<V, P> {

    public interface ViewDef extends View {
        // Each view must implement a close button.
        HasClickHandlers getCloseButton();
    }

    public AbstractOverlayPresenter(EventBus eventBus, V view, P proxy) {
        super(eventBus, view, proxy, MainContentPresenter.TYPE_SetContent);
    }

    @Override
    public void onBind() {
        super.onBind();
        registerHandler(((ViewDef) getView()).getCloseButton().addClickHandler(e ->
            AbstractOverlayPresenter.this.removeFromParentSlot()
        ));
    }
}
