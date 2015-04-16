package org.ovirt.engine.ui.common.widget;

/**
 * Widgets that implement this interface have a user access policy associated with them.
 * <p>
 *     Inaccessible widgets should be hidden. Usually using
 *     {@link com.google.gwt.user.client.ui.Widget#setVisible(boolean)}
 * </p>
 */
public interface HasAccess {

    /**
     * Checks whether or not the current user has the right to access this widget.
     *
     * @return {@code true} if the user can access this widget, {@code false} otherwise.
     */
    boolean isAccessible();

    /**
     * Sets the accessibility of the widget for the current user.
     *
     * @param accessible
     *            {@code true} if the user can access this widget, {@code false} otherwise.
     */
    void setAccessible(boolean accessible);

}
