package org.ovirt.engine.ui.webadmin.widget.action;

import java.util.List;

import org.ovirt.engine.ui.webadmin.widget.HasAccess;

import com.google.gwt.event.logical.shared.HasInitializeHandlers;
import com.google.gwt.safehtml.shared.SafeHtml;

/**
 * Describes a button rendered within an {@link AbstractActionPanel}.
 *
 * @param <T>
 *            Action panel item type.
 */
public interface ActionButtonDefinition<T> extends HasAccess, HasInitializeHandlers {

    /**
     * Action button click event callback.
     *
     * @param selectedItems
     *            Items currently selected in the {@link AbstractActionPanel}.
     */
    void onClick(List<T> selectedItems);

    /**
     * Checks whether or not this action button should be enabled for the given selection.
     *
     * @param selectedItems
     *            Items currently selected in the {@link AbstractActionPanel}.
     */
    boolean isEnabled(List<T> selectedItems);

    /**
     * Returns the content to show when this button is enabled.
     */
    SafeHtml getEnabledHtml();

    /**
     * Returns the content to show when this button is disabled.
     */
    SafeHtml getDisabledHtml();

    /**
     * Returns the title of this button, used in context menus and as the button tooltip.
     */
    String getTitle();

    /**
     * Returns the ID that uniquely identifies this button.
     */
    String getUniqueId();

    /**
     * Indicates whether the action is implemented or not, This is only relevant for the first tech-preview of webadmin
     * where not all buttons may be implemented TODO: This is temporary and should be cleaned up when WebAdmin will be
     * fully implemented!
     *
     * @param isImplemented
     *            whether this action is available or not
     * @return whether action is available or not
     */
    boolean isImplemented();

    /**
     * If action is not available, then this property indicates whether the action is available in user portal or not
     * This is only affecting the message that will be displaying when the button is clicked. TODO: This is temporary
     * and should be cleaned up when WebAdmin will be fully implemented!
     *
     * @param isImplInUserPortal
     *            whether this action is implemented in user portal or not
     * @return true/false
     */
    boolean isImplInUserPortal();

    /**
     * Indicates whether this action button is available only from the corresponding context menu.
     */
    boolean isAvailableOnlyFromContext();

    /**
     * Updates the state of this action button.
     */
    void update();

}
