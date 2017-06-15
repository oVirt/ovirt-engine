package org.ovirt.engine.ui.webadmin.section.main.presenter.popup;

import org.ovirt.engine.ui.common.presenter.AbstractPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.CloseDynamicPopupEvent.CloseDynamicPopupHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.SetDynamicPopupContentUrlEvent.SetDynamicPopupContentUrlHandler;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

/**
 * Implements a dialog with content loaded from the given URL.
 * <p>
 * This presenter widget is bound as non-singleton to allow showing multiple dialog instances.
 */
public class DynamicUrlContentPopupPresenterWidget extends AbstractPopupPresenterWidget<DynamicUrlContentPopupPresenterWidget.ViewDef> implements SetDynamicPopupContentUrlHandler, CloseDynamicPopupHandler {

    public interface ViewDef extends AbstractPopupPresenterWidget.ViewDef {

        void init(String title, String width, String height, boolean closeIconVisible);

        void setContentUrl(String contentUrl);

        HasClickHandlers addFooterButton(String label);

    }

    private String dialogToken;
    private boolean closeOnEscKey;

    @Inject
    public DynamicUrlContentPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    protected void onBind() {
        super.onBind();
        registerHandler(getEventBus().addHandler(SetDynamicPopupContentUrlEvent.getType(), this));
        registerHandler(getEventBus().addHandler(CloseDynamicPopupEvent.getType(), this));
    }

    @Override
    public void onSetDynamicPopupContentUrl(SetDynamicPopupContentUrlEvent event) {
        if (dialogTokenMatches(event.getDialogToken())) {
            setContentUrl(event.getContentUrl());
        }
    }

    @Override
    public void onCloseDynamicPopup(CloseDynamicPopupEvent event) {
        if (dialogTokenMatches(event.getDialogToken())) {
            onClose();
        }
    }

    boolean dialogTokenMatches(String value) {
        return dialogToken != null && dialogToken.equals(value);
    }

    @Override
    protected void handleEscapeKey() {
        if (closeOnEscKey) {
            onClose();
        }
    }

    public void init(String dialogToken, String title, String width, String height,
            boolean closeIconVisible, boolean closeOnEscKey) {
        this.dialogToken = dialogToken;
        this.closeOnEscKey = closeOnEscKey;
        getView().init(title, width, height, closeIconVisible);
    }

    public void setContentUrl(String contentUrl) {
        getView().setContentUrl(contentUrl);
    }

    public void addFooterButton(String label, ClickHandler clickHandler) {
        registerHandler(getView().addFooterButton(label).addClickHandler(clickHandler));
    }

}
