package org.ovirt.engine.ui.webadmin.plugin.ui;

import org.ovirt.engine.ui.webadmin.plugin.jsni.JsFunction;
import org.ovirt.engine.ui.webadmin.plugin.jsni.JsInterfaceObject;

/**
 * Overlay type for action button interface JS object.
 */
public final class ActionButtonInterface extends JsInterfaceObject {

    protected ActionButtonInterface() {
    }

    public JsFunction onClick() {
        return getFunction("onClick"); //$NON-NLS-1$
    }

    public JsFunction isEnabled() {
        return getFunction("isEnabled"); //$NON-NLS-1$
    }

    public JsFunction isAccessible() {
        return getFunction("isAccessible"); //$NON-NLS-1$
    }

}
