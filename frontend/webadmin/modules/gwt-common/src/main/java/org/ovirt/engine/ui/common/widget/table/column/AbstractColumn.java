package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.idhandler.CellWithElementId;

import com.google.gwt.user.cellview.client.Column;

/**
 * Base class for Columns that work with Cells that use the Element ID framework.
 */
public abstract class AbstractColumn<T, C> extends Column<T, C> implements ColumnWithElementId {

    public AbstractColumn(CellWithElementId<C> cell) {
        super(cell);
    }

    public CellWithElementId<C> getCell() {
        return (CellWithElementId<C>) super.getCell();
    }

    @Override
    public void configureElementId(String elementIdPrefix, String columnId) {
        getCell().setElementIdPrefix(elementIdPrefix);
        getCell().setColumnId(columnId);
    }

}
