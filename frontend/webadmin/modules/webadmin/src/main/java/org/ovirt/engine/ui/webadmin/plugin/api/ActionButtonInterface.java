package org.ovirt.engine.ui.webadmin.plugin.api;

import org.ovirt.engine.ui.common.widget.action.CommandLocation;
import org.ovirt.engine.ui.webadmin.plugin.jsni.JsFunction;
import org.ovirt.engine.ui.webadmin.plugin.jsni.JsInterfaceObject;

/**
 * Represents action button interface JS object.
 */
public final class ActionButtonInterface extends JsInterfaceObject {

    protected ActionButtonInterface() {
    }

    /**
     * Called when the user clicks the action button.
     * <p>
     * Default return value: N/A
     */
    public JsFunction onClick() {
        return getFunction("onClick"); //$NON-NLS-1$
    }

    /**
     * Controls whether the action button is enabled (clickable).
     * <p>
     * Default return value: {@code true}
     */
    public JsFunction isEnabled() {
        return getFunction("isEnabled"); //$NON-NLS-1$
    }

    /**
     * Controls whether the action button is accessible (visible).
     * <p>
     * Default return value: {@code true}
     */
    public JsFunction isAccessible() {
        return getFunction("isAccessible"); //$NON-NLS-1$
    }

    /**
     * Returns the location of the action button, as defined in {@link CommandLocation}.
     * <p>
     * Default return value: {@link CommandLocation#ContextAndToolBar ContextAndToolBar}
     */
    public CommandLocation getLocation() {
        return getValueAsEnum("location", CommandLocation.class, CommandLocation.ContextAndToolBar); //$NON-NLS-1$
    }

}
