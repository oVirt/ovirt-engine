package org.ovirt.engine.ui.webadmin.gin;

import org.ovirt.engine.ui.common.auth.LoggedInGatekeeper;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.widget.tab.WebadminMenuLayout;

import com.gwtplatform.mvp.client.annotations.DefaultGatekeeper;

/**
 * WebAdmin {@code Ginjector} extension interface.
 */
public interface ClientGinjectorExtension {

    // Core GWTP components

    @DefaultGatekeeper
    LoggedInGatekeeper getDefaultGatekeeper();

    // Application-level components

    ApplicationConstants getApplicationConstants();

    // WebadminMenuLayout
    WebadminMenuLayout getWebadminMenuLayout();
}
