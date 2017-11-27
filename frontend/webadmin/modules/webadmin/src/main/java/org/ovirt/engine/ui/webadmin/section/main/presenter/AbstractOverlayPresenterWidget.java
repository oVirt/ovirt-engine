package org.ovirt.engine.ui.webadmin.section.main.presenter;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public abstract class AbstractOverlayPresenterWidget<V extends AbstractOverlayPresenterWidget.ViewDef> extends PresenterWidget<V> {

    private PresenterWidget<?> currentPlaceWidget;

    public interface ViewDef extends View {
        // Each view must implement a close button.
        HasClickHandlers getCloseButton();
    }

    public AbstractOverlayPresenterWidget(EventBus eventBus, V view) {
        super(eventBus, view);
    }

    @Override
    public void onBind() {
        super.onBind();
        registerHandler(getView().getCloseButton().addClickHandler(e ->
            RevealOverlayContentEvent.fire(this, new RevealOverlayContentEvent(null))
        ));
    }

    public PresenterWidget<?> getCurrentPlaceWidget() {
        return currentPlaceWidget;
    }

    public void setCurrentPlaceWidget(PresenterWidget<?> currentPlaceWidget) {
        this.currentPlaceWidget = currentPlaceWidget;
    }
}
