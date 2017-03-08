package org.ovirt.engine.ui.common.widget.table;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Classes that implement this interface provide access to {@link ActionTable} widget.
 *
 * @param <T>
 *            Table row data type.
 */
public interface HasActionTable<T> {

    /**
     * Returns the action table widget or {@code null} if this widget isn't available.
     */
    ActionTable<T> getTable();

    /**
     * Returns the widget containing the {@code ActionTable}.
     * @return The widget with the action table.
     */
    IsWidget getTableContainer();
}
