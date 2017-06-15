package org.ovirt.engine.ui.webadmin.plugin.api;

import org.ovirt.engine.ui.webadmin.plugin.jsni.JsObjectWithProperties;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * Represents modal dialog options object.
 */
public final class DialogOptions extends JsObjectWithProperties {

    protected DialogOptions() {
    }

    /**
     * Returns the buttons to use in the dialog.
     * <p>
     * Default return value: empty array
     */
    public JsArray<DialogButtonInterface> getButtons() {
        return getValueAsArray("buttons", //$NON-NLS-1$
                JavaScriptObject.createArray().<JsArray<DialogButtonInterface>> cast());
    }

    /**
     * Controls whether the dialog close icon is visible.
     * <p>
     * Default return value: {@code true}
     */
    public Boolean getCloseIconVisible() {
        return getValueAsBoolean("closeIconVisible", true); //$NON-NLS-1$
    }

    /**
     * Controls whether the dialog can be closed with Escape key.
     * <p>
     * Default return value: {@code true}
     */
    public Boolean getCloseOnEscKey() {
        return getValueAsBoolean("closeOnEscKey", true); //$NON-NLS-1$
    }

}
