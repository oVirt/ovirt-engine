package org.ovirt.engine.ui.common.widget;

import java.util.List;

/**
 * A widget that implements this interface can be put in an "enabled"
 * or "disabled" state.
 */
public interface HasEnabled {

    /**
     * Returns true if the widget is enabled, false if not.
     */
    boolean isEnabled();

    /**
     * Sets whether this widget is enabled.
     *
     * @param enabled <code>true</code> to enable the widget, <code>false</code>
     *          to disable it
     */
    void setEnabled(boolean enabled);

    /**
     * Sets whether this widget is enabled.
     *
     * @param enabled <code>true</code> to enable the widget, <code>false</code>
     *          to disable it
     *
     * @param disabilityHints
     *           Disability hints describing reasons why this widget is disabled (can be empty or {@code null}).
     */
    void setEnabled(boolean enabled, List<String> disabilityHints);
}
