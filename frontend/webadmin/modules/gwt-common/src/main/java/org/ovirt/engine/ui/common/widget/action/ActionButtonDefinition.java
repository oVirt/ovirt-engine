package org.ovirt.engine.ui.common.widget.action;

import java.util.List;

import com.google.gwt.dom.client.Style.HasCssName;
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
     * Get the Css name of the icon.
     * @return The HasCssName class of the icon.
     */
    HasCssName getIcon();

    /**
     * Returns the text of this button, used in context menus.
     */
    String getText();

    /**
     * Returns the ID that uniquely identifies this button or {@code null} if not available.
     */
    String getUniqueId();

    /**
     * Updates the state of this action button.
     */
    void update();

    /**
     * Indicates whether this action button has a title action.
     */
    boolean isSubTitledAction();

    /**
     * Returns the tooltip for this action button.
     */
    SafeHtml getTooltip();

    /**
     * Returns the tooltip for the context menu item representing this action button.
     */
    SafeHtml getMenuItemTooltip();

    /**
     * Returns the index of this action button, denoting its relative position within
     * the action panel.
     */
    int getIndex();

}
