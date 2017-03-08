package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.webadmin.section.main.presenter.overlay.OverlayPresenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.Proxy;

public abstract class AbstractOverlayPresenter<V extends View, P extends Proxy<?>> extends Presenter<V, P>
    implements OverlayPresenter {

    public interface ViewDef extends View {
        // Each view must implement a close button.
        HasClickHandlers getCloseButton();
    }

    public AbstractOverlayPresenter(EventBus eventBus, V view, P proxy) {
        super(eventBus, view, proxy, MainContentPresenter.TYPE_SetOverlayContent);
    }

    @Override
    public void onBind() {
        super.onBind();
        registerHandler(((ViewDef) getView()).getCloseButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                UpdateMainContentLayoutEvent.fire(AbstractOverlayPresenter.this,
                        UpdateMainContentLayout.ContentDisplayType.RESTORE, getOverlayType());
            }

        }));
    }
}
