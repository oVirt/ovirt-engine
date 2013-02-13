package org.ovirt.engine.ui.common.widget;

import com.google.gwt.user.client.ui.HasEnabled;

/**
 * Extends the {@link HasEnabled} interface, allowing widgets to be disabled while providing hint describing the reason
 * for disabling them.
 */
public interface HasEnabledWithHints extends HasEnabled {

    /**
     * Disables this widget, providing an disability hint.
     *
     * @param disabilityHint
     *            Disability hint describing reason why this widget is disabled (can be {@code null}).
     */
    void disable(String disabilityHint);

}
