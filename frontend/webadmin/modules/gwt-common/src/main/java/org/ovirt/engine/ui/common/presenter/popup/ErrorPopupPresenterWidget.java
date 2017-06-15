package org.ovirt.engine.ui.common.presenter.popup;

import org.ovirt.engine.ui.common.presenter.AbstractPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

/**
 * Implements the default error dialog.
 */
public class ErrorPopupPresenterWidget extends AbstractPopupPresenterWidget<ErrorPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractPopupPresenterWidget.ViewDef {

        void setErrorMessage(String errorMessage);

    }

    @Inject
    public ErrorPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    public void prepare(String errorMessage) {
        getView().setErrorMessage(errorMessage);
    }

    @Override
    protected void handleEnterKey() {
        onClose();
    }

}
