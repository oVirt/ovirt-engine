package org.ovirt.engine.ui.common.system;

import org.ovirt.engine.ui.common.presenter.popup.ErrorPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.ErrorPopupManager;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.proxy.RevealRootPopupContentEvent;

/**
 * Convenience class used to reveal {@link ErrorPopupPresenterWidget} as a global popup.
 */
public class ErrorPopupManagerImpl implements HasHandlers, ErrorPopupManager {

    private final EventBus eventBus;
    private final Provider<ErrorPopupPresenterWidget> errorPopupProvider;

    @Inject
    public ErrorPopupManagerImpl(EventBus eventBus, Provider<ErrorPopupPresenterWidget> errorPopupProvider) {
        this.eventBus = eventBus;
        this.errorPopupProvider = errorPopupProvider;
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEvent(event);
    }

    @Override
    public void show(String errorMessage) {
        ErrorPopupPresenterWidget errorPopup = errorPopupProvider.get();
        errorPopup.prepare(errorMessage);
        RevealRootPopupContentEvent.fire(this, errorPopup);
    }

}
