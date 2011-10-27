package org.ovirt.engine.ui.webadmin.presenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

/**
 * Default error popup presenter.
 */
public class ErrorPopupPresenterWidget extends PresenterWidget<ErrorPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends PopupView {

        void setErrorMessage(String errorMessage);

        HasClickHandlers getCloseButton();

    }

    @Inject
    public ErrorPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    protected void onBind() {
        super.onBind();

        registerHandler(getView().getCloseButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getView().hide();
            }
        }));
    }

    public void prepare(String errorMessage) {
        getView().setErrorMessage(errorMessage);
    }

}
