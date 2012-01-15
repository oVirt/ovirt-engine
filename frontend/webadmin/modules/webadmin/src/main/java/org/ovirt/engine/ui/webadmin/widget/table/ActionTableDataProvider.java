package org.ovirt.engine.ui.webadmin.widget.table;

import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;

/**
 * General contract for {@link com.google.gwt.view.client.AbstractActionTable} data providers.
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
    public void addDataDisplay(HasData<T> display);

}
