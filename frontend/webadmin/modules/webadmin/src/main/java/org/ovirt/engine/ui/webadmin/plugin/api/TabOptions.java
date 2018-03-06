package org.ovirt.engine.ui.webadmin.plugin.api;

import org.ovirt.engine.ui.webadmin.plugin.jsni.JsObjectWithProperties;

/**
 * Represents dynamic tab options object.
 */
public final class TabOptions extends JsObjectWithProperties {

    protected TabOptions() {
    }

    /**
     * Returns the priority of the tab, denoting its relative position within the tab panel.
     * <p>
     * Standard main and sub tabs typically have their priority starting at 0 (left-most tab)
     * and incremented by 1 for each next tab.
     * <p>
     * Default return value: {@code Double.MAX_VALUE}
     */
    public Double getPriority() {
        return getValueAsDouble("priority", Double.MAX_VALUE); //$NON-NLS-1$
    }

    /**
     * If true the passed in place will be the default place when a user logs in if there are no
     * places specified in the browser URL bar.
     * Default return value: {@code false}
     */
    public Boolean getDefaultPlace() {
        return getValueAsBoolean("defaultPlace", false); //$NON-NLS-1$
    }

    /**
     * Returns the search prefix associated with the tab.
     * <p>
     * Applies only to main tabs.
     * <p>
     * Default return value: {@code null}
     */
    public String getSearchPrefix() {
        return getValueAsString("searchPrefix", null); //$NON-NLS-1$
    }

    /**
     * Returns the icon as a css class name. The acceptable icons are either font awesome icons or patternfly
     * icons. For example fa-user for the user icon.
     * @return A string representing the name of the icon css class name.
     */
    public String getIcon() {
        return getValueAsString("icon", null); //$NON-NLS-1$
    }

}
