package org.ovirt.engine.ui.userportal.gin;

import org.ovirt.engine.ui.common.auth.LoggedInGatekeeper;
import org.ovirt.engine.ui.common.gin.BaseClientGinjector;
import org.ovirt.engine.ui.userportal.auth.LoggedInExtendedPlaceGatekeeper;

import com.google.gwt.inject.client.GinModules;
import com.gwtplatform.mvp.client.annotations.DefaultGatekeeper;

/**
 * Client-side injector configuration used to bootstrap GIN.
 */
@GinModules({ SystemModule.class, PresenterModule.class, UiCommonModule.class, UtilsModule.class })
public interface ClientGinjector extends BaseClientGinjector, ManagedComponents {

    @DefaultGatekeeper
    LoggedInExtendedPlaceGatekeeper getDefaultGatekeeper();

    LoggedInGatekeeper getLoggedInGatekeeper();

}
