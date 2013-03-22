package org.ovirt.engine.ui.webadmin.plugin.api;

import org.ovirt.engine.ui.webadmin.plugin.jsni.JsObjectWithProperties;

/**
 * Represents dynamic tab options object.
 */
public final class TabOptions extends JsObjectWithProperties {

    protected TabOptions() {
    }

    /**
     * Controls whether the tab is aligned to the right side of tab panel.
     * <p>
     * Default return value: {@code false}
     */
    public Boolean getAlignRight() {
        return getValueAsBoolean("alignRight", false); //$NON-NLS-1$
    }

}
