package org.ovirt.engine.ui.webadmin.widget.table;

import org.ovirt.engine.ui.webadmin.idhandler.HasElementId;
import org.ovirt.engine.ui.webadmin.widget.table.column.ColumnWithElementId;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ProvidesKey;

/**
 * A cell table which adds the support for setting up the elementId. Note that it doesn't do it automatically, the
 * configureElementId() has to be called on it.
 */
public abstract class ElementIdCellTable<T> extends CellTable<T> implements HasElementId {

    private String elementId = DOM.createUniqueId();

    public ElementIdCellTable() {
        super();
    }

    public ElementIdCellTable(int pageSize, ProvidesKey<T> keyProvider) {
        super(pageSize, keyProvider);
    }

    public ElementIdCellTable(int pageSize,
            com.google.gwt.user.cellview.client.CellTable.Resources resources,
            ProvidesKey<T> keyProvider,
            Widget loadingIndicator) {
        super(pageSize, resources, keyProvider, loadingIndicator);
    }

    public ElementIdCellTable(int pageSize,
            com.google.gwt.user.cellview.client.CellTable.Resources resources,
            ProvidesKey<T> keyProvider) {
        super(pageSize, resources, keyProvider);
    }

    public ElementIdCellTable(int pageSize, com.google.gwt.user.cellview.client.CellTable.Resources resources) {
        super(pageSize, resources);
    }

    public ElementIdCellTable(int pageSize) {
        super(pageSize);
    }

    public ElementIdCellTable(ProvidesKey<T> keyProvider) {
        super(keyProvider);
    }

    /**
     * Sets up the element id for the specific column, if it is a ColumnWithElementId
     */
    protected void configureElementId(Column<T, ?> column) {
        configureElementId(column, null);
    }

    /**
     * Sets up the element id for the specific column, if it is a ColumnWithElementId
     */
    protected void configureElementId(Column<T, ?> column, String columnId) {
        if (column instanceof ColumnWithElementId) {
            ((ColumnWithElementId) column).configureElementId(elementId, columnId);
        }
    }

    @Override
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public String getElementId() {
        return elementId;
    }

}
