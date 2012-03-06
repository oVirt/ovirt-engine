package org.ovirt.engine.ui.common.system;

import org.ovirt.engine.ui.common.presenter.popup.ErrorPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.RevealRootPopupContentEvent;

/**
 * Convenience class used to reveal {@link ErrorPopupPresenterWidget} as a global popup.
 */
public class ErrorPopupManager implements HasHandlers {

    private final EventBus eventBus;
    private final ErrorPopupPresenterWidget errorPopup;

    private boolean showPopups;

    @Inject
    public ErrorPopupManager(EventBus eventBus, ErrorPopupPresenterWidget errorPopup) {
        this.eventBus = eventBus;
        this.errorPopup = errorPopup;
        this.showPopups = true;
    }

    public void setShowPopups(boolean showPopups) {
        this.showPopups = showPopups;
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEvent(event);
    }

    public void show(String errorMessage) {
        if (showPopups) {
            errorPopup.prepare(errorMessage);
            RevealRootPopupContentEvent.fire(this, errorPopup);
        }
    }

}
