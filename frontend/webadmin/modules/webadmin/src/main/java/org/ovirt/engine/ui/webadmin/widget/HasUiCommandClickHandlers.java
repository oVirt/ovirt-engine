package org.ovirt.engine.ui.webadmin.widget;

import org.ovirt.engine.ui.uicommonweb.UICommand;

import com.google.gwt.event.dom.client.HasClickHandlers;

/**
 * Widgets that implement this interface provide {@link com.google.gwt.event.dom.client.ClickHandler} registration for executing UiCommon
 * {@linkplain UICommand commands}.
 */
public interface HasUiCommandClickHandlers extends HasClickHandlers {

    /**
     * Returns the command associated with this widget.
     */
    UICommand getCommand();

    /**
     * Sets the command associated with this widget.
     */
    void setCommand(UICommand command);

}
