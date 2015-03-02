package org.ovirt.engine.ui.common.widget.action;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ToggleButton;

/**
 * Represents a button rendered within an {@link ActionPanel}.
 */
public interface ActionButton extends IsWidget, HasClickHandlers, HasEnabled {

    /**
     * Sets the tooltip of this button.
     */
    void setTooltipText(String title);

    /**
     * Sets the HTML content presented when this button is enabled.
     */
    void setEnabledHtml(SafeHtml html);

    /**
     * Sets the HTML content presented when this button is disabled.
     */
    void setDisabledHtml(SafeHtml html);

    /**
     * Returns this button represented as {@link ToggleButton} widget.
     */
    ToggleButton asToggleButton();

}
