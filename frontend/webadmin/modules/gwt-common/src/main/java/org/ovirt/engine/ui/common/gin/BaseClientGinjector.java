package org.ovirt.engine.ui.common.gin;

import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.ClientAgentType;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.Ginjector;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.proxy.Gatekeeper;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

/**
 * Common GIN injector interface.
 */
public interface BaseClientGinjector extends Ginjector {

    // Core GWTP components

    EventBus getEventBus();

    PlaceManager getPlaceManager();

    Gatekeeper getDefaultGatekeeper();

    // Core application components

    ClientAgentType getClientAgentType();

    // PresenterWidgets

    Provider<DefaultConfirmationPopupPresenterWidget> getDefaultConfirmationPopupProvider();

    Provider<RemoveConfirmationPopupPresenterWidget> getRemoveConfirmPopupProvider();

}
