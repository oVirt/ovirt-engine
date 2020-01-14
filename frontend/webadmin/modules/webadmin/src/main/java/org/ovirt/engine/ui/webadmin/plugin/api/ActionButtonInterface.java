package org.ovirt.engine.ui.webadmin.plugin.api;

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
     * Returns the index of the action button, denoting its relative position within
     * the action panel.
     * <p>
     * Action buttons have their index starting at 0 (left-most button) and incremented
     * by 1 for each next button.
     * <p>
     * Default return value: {@code Integer.MAX_VALUE}
     */
    public Integer getIndex() {
        return Math.max(0, getValueAsInteger("index", Integer.MAX_VALUE)); //$NON-NLS-1$
    }

    /**
     * Determines if the action button will be a menu item in the 'more items' menu of
     * the action panel.
     * <p>
     * Default return value: {@code false}
     */
    public Boolean isInMoreMenu() {
        return getValueAsBoolean("moreMenu", false); //$NON-NLS-1$
    }

    /**
     * Returns the id of the action button.
     *
     * <p>
     * Default return value: {@code null}
     */
    public String getId() {
        return getValueAsString("id", null); //$NON-NLS-1$
    }

}
