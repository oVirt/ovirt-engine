package org.ovirt.engine.ui.common.widget.table;

import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;

/**
 * General contract for {@link AbstractActionTable} data providers.
 *
 * @param <T>
 *            Table row data type.
 *
 * @see com.google.gwt.view.client.AbstractDataProvider
 */
public interface ActionTableDataProvider<T> extends ProvidesKey<T>, HasPaging {

    /**
     * Adds a data display to this provider.
     */
    void addDataDisplay(HasData<T> display);

    /**
     * return the table items count
     * @return String (1-40)
     */
    String getItemsCount();

    /**
     * Get the lower end count as a number
     * @return The lower end of the total count
     */
    int getFromCount();

    /**
     * Get the higher end count as a number
     * @return The higher end of the total count
     */
    int getToCount();

}
