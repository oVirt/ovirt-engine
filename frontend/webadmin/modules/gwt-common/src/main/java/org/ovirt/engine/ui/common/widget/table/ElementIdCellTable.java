package org.ovirt.engine.ui.common.widget.table;

import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.idhandler.ProvidesElementId;
import org.ovirt.engine.ui.common.widget.table.column.ColumnWithElementId;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ProvidesKey;

/**
 * A {@link CellTable} which adds support for configuring column DOM element IDs through {@link ColumnWithElementId}
 * interface.
 *
 * @param <T>
 *            Table row data type.
 */
public class ElementIdCellTable<T> extends ColumnResizeCellTable<T> implements HasElementId, ProvidesElementId {

    private String elementId = DOM.createUniqueId();

    public ElementIdCellTable() {
        super();
    }

    public ElementIdCellTable(int pageSize, ProvidesKey<T> keyProvider) {
        super(pageSize, keyProvider);
    }

    public ElementIdCellTable(int pageSize, DataGrid.Resources resources,
            ProvidesKey<T> keyProvider, Widget loadingIndicator) {
        super(pageSize, resources, keyProvider, loadingIndicator);
    }

    public ElementIdCellTable(int pageSize, DataGrid.Resources resources,
            ProvidesKey<T> keyProvider) {
        super(pageSize, resources, keyProvider);
    }

    public ElementIdCellTable(int pageSize, DataGrid.Resources resources) {
        super(pageSize, resources);
    }

    public ElementIdCellTable(int pageSize) {
        super(pageSize);
    }

    public ElementIdCellTable(ProvidesKey<T> keyProvider) {
        super(keyProvider);
    }

    @Override
    public void insertColumn(int beforeIndex, Column<T, ?> col, Header<?> header, Header<?> footer) {
        super.insertColumn(beforeIndex, col, header, footer);
        configureElementId(col);
    }

    /**
     * Sets up the element ID for the given column, if the column implements {@link ColumnWithElementId}.
     */
    protected void configureElementId(Column<T, ?> column) {
        configureElementId(column, null);
    }

    /**
     * Sets up the element ID for the given column, if the column implements {@link ColumnWithElementId}.
     * <p>
     * This method overrides the default column ID that will be part of the resulting DOM element ID.
     */
    void configureElementId(Column<T, ?> column, String columnId) {
        if (column instanceof ColumnWithElementId) {
            ((ColumnWithElementId) column).configureElementId(elementId, columnId);
        }
    }

    @Override
    public void setElementId(String elementId) {
        this.elementId = elementId;
        // We can only populate the swapped list after the id is set since we use the id to generate the key
        // to look up the swapped list. If the elementId is not set, then the map will remain empty and the
        // order cannot be overriden.
        popuplateSwappedList();
    }

    @Override
    public String getElementId() {
        return elementId;
    }

}
