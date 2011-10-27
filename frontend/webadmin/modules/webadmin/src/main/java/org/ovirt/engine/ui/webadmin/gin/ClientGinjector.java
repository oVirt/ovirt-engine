package org.ovirt.engine.ui.webadmin.gin;

import org.ovirt.engine.ui.webadmin.auth.LoggedInGatekeeper;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import com.gwtplatform.mvp.client.annotations.DefaultGatekeeper;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

/**
 * Client-side injector configuration used to bootstrap GIN.
 */
@GinModules({ SystemModule.class, PresenterModule.class, UiCommonModule.class })
public interface ClientGinjector extends Ginjector, ManagedComponents {

    EventBus getEventBus();

    PlaceManager getPlaceManager();

    @DefaultGatekeeper
    LoggedInGatekeeper getDefaultGatekeeper();

}
