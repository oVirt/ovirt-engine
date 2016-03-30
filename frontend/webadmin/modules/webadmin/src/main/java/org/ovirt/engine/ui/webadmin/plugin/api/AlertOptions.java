package org.ovirt.engine.ui.webadmin.plugin.api;

import org.ovirt.engine.ui.webadmin.plugin.jsni.JsObjectWithProperties;

/**
 * Represents application-wide alert options object.
 */
public final class AlertOptions extends JsObjectWithProperties {

    protected AlertOptions() {
    }

    /**
     * Returns the number of milliseconds after which the alert should be closed.
     * <p>
     * A negative value is functionally equivalent to 0 (auto-hide disabled).
     * <p>
     * Default return value: 0
     */
    public Double getAutoHideMs() {
        return getValueAsDouble("autoHideMs", 0d); //$NON-NLS-1$
    }

}
