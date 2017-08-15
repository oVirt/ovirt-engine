package org.ovirt.engine.ui.webadmin.gin;

import org.ovirt.engine.ui.common.auth.LoggedInGatekeeper;

import com.gwtplatform.mvp.client.annotations.DefaultGatekeeper;

/**
 * WebAdmin {@code Ginjector} extension interface.
 * <p>
 * Static {@link com.gwtplatform.mvp.client.annotations.TabInfo} methods
 * have their arguments resolved through getters defined in this interface.
 * <p>
 * This interface is effectively a programmatic way to obtain dependencies
 * managed by GIN, complementing the usual declarative (constructor, method,
 * field) injection via {@code @Inject} annotation.
 */
public interface ClientGinjectorExtension {

    // Core GWTP components

    @DefaultGatekeeper
    LoggedInGatekeeper getDefaultGatekeeper();

}
