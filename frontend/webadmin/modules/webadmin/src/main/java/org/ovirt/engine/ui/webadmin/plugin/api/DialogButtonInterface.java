package org.ovirt.engine.ui.webadmin.plugin.api;

import org.ovirt.engine.ui.webadmin.plugin.jsni.JsFunction;
import org.ovirt.engine.ui.webadmin.plugin.jsni.JsInterfaceObject;

/**
 * Represents modal dialog button interface JS object.
 */
public final class DialogButtonInterface extends JsInterfaceObject {

    protected DialogButtonInterface() {
    }

    /**
     * Returns the label of the dialog button.
     * <p>
     * Default return value: empty string
     */
    public String getLabel() {
        return getValueAsString("label", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Called when the user clicks the dialog button.
     * <p>
     * Default return value: N/A
     */
    public JsFunction onClick() {
        return getFunction("onClick"); //$NON-NLS-1$
    }

}
