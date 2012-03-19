package org.ovirt.engine.ui.common.widget.action;

import java.util.List;

import org.ovirt.engine.ui.common.widget.HasAccess;

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
     * Indicates whether the functionality bound to this action button is implemented or not.
     * <p>
     * This is only relevant for a tech-preview of the given application, where not all buttons might be implemented.
     * <p>
     * TODO: This is temporary and should be cleaned up when WebAdmin will be fully implemented!
     *
     * @return Whether the functionality bound to this action button is implemented or not.
     */
    boolean isImplemented();

    /**
     * If {@link #isImplemented()} is {@code true}, this method indicates whether this action button is available in
     * UserPortal or not.
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
     *
     *  Indicates whether this action button has a title action.
     */
    boolean isSubTitledAction();

    String getToolTip();

    /**
    *
    *  Indicates whether this action button is visible.
    */
    boolean isVisible(List<T> selectedItems);

    String getCustomToolTip();
}
