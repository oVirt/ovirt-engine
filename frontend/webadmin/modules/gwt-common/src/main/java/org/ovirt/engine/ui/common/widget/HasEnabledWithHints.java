package org.ovirt.engine.ui.common.widget;

import java.util.List;

import com.google.gwt.user.client.ui.HasEnabled;

/**
 * Extends the {@link HasEnabled} interface, allowing widgets to be disabled while providing hints describing reasons
 * for disabling them.
 */
public interface HasEnabledWithHints extends HasEnabled {

    /**
     * Disables this widget, providing an optional list of disability hints.
     *
     * @param disabilityHints
     *            Disability hints describing reasons why this widget is disabled (can be empty or {@code null}).
     */
    void disable(List<String> disabilityHints);

}
