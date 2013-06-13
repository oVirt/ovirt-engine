package org.ovirt.engine.ui.common.widget.action;

import java.util.List;

import com.google.gwt.event.logical.shared.HasInitializeHandlers;
import com.google.gwt.safehtml.shared.SafeHtml;

/**
 * Describes a button rendered within an {@link ActionPanel}.
 *
 * @param <T>
 *            Action panel item type.
 */
public interface ActionButtonDefinition<T> extends HasInitializeHandlers {

    /**
     * Action button click event callback.
     */
    void onClick(List<T> selectedItems);

    /**
     * Checks whether or not this action button should be enabled for the given selection.
     */
    boolean isEnabled(List<T> selectedItems);

    /**
     * Checks whether or not the current user has the right to access this action button.
     */
    boolean isAccessible(List<T> selectedItems);

    /**
     * Indicates whether this action button is visible.
     */
    boolean isVisible(List<T> selectedItems);

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
     * Returns the ID that uniquely identifies this button or {@code null} if not available.
     */
    String getUniqueId();

    /**
     * Indicates whether the functionality bound to this action button is implemented.
     * <p>
     * This is only relevant for a tech-preview of the given application, where not all buttons might be implemented.
     * <p>
     * TODO: This is temporary and should be cleaned up when WebAdmin will be fully implemented!
     *
     * @return Whether the functionality bound to this action button is implemented or not.
     */
    boolean isImplemented();

    /**
     * If {@link #isImplemented} is {@code true}, this method indicates whether this action button is available in
     * UserPortal.
     * <p>
     * This is only affecting the message that will be shown when the button is clicked.
     * <p>
     * TODO: This is temporary and should be cleaned up when WebAdmin will be fully implemented!
     *
     * @return Whether this action button is available in UserPortal or not.
     */
    boolean isImplInUserPortal();

    /**
     * Indicates whether this action button is available only from the corresponding context menu.
     */
    CommandLocation getCommandLocation();

    /**
     * Updates the state of this action button.
     */
    void update();

    /**
     * Indicates whether this action button has a title action.
     */
    boolean isSubTitledAction();

    /**
     * Returns the tool-tip for this action button or {@code null} to use button title value.
     */
    String getButtonToolTip();

    /**
     * Returns the tool-tip for the context menu item representing this action button.
     */
    String getMenuItemToolTip();

}
