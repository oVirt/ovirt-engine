package org.ovirt.engine.ui.webadmin.widget.table;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Represents a button rendered within an {@link AbstractActionTable}'s action panel.
 */
public interface ActionButton extends IsWidget, HasClickHandlers, HasEnabled {

    /**
     * Set the tooltip title
     */
    public void setTitle(String title);

    /**
     * Set the HTML for the Button in its Enabled state
     * 
     * @param html
     */
    public void setEnabledHtml(SafeHtml html);

    /**
     * Set the HTML for the Button in its Disabled state
     * 
     * @param html
     */
    public void setDisabledHtml(SafeHtml html);

}
