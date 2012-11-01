package org.ovirt.engine.ui.webadmin.gin;

import org.ovirt.engine.ui.common.auth.LoggedInGatekeeper;
import org.ovirt.engine.ui.common.gin.BaseClientGinjector;

import com.google.gwt.inject.client.GinModules;
import com.gwtplatform.mvp.client.annotations.DefaultGatekeeper;

/**
 * Client-side injector configuration used to bootstrap GIN.
 */
@GinModules({ SystemModule.class, PresenterModule.class, UiCommonModule.class, PluginModule.class })
public interface ClientGinjector extends BaseClientGinjector, ManagedComponents {

    @Override
    @DefaultGatekeeper
    LoggedInGatekeeper getDefaultGatekeeper();

}
