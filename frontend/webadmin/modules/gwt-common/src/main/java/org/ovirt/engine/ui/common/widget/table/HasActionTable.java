package org.ovirt.engine.ui.common.widget.table;

/**
 * Classes that implement this interface provide access to {@link ActionTable} widget.
 *
 * @param <T>
 *            Table row data type.
 */
public interface HasActionTable<T> {

    /**
     * Returns the action table widget.
     */
    ActionTable<T> getTable();

}
