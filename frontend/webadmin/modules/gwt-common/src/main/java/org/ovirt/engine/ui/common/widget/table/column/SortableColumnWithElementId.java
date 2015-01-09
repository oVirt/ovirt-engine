package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.cell.CellWithElementId;

/**
 * A {@link SortableColumn} that implements ColumnWithElementId (has an id).
 *
 * @param <T>
 *            Table row data type.
 * @param <C>
 *            Cell data type.
 */
public abstract class SortableColumnWithElementId<T, C> extends SortableColumn<T, C> implements ColumnWithElementId {

    public SortableColumnWithElementId(CellWithElementId<C> cell) {
        super(cell);
    }

    @Override
    public CellWithElementId<C> getCell() {
        return (CellWithElementId<C>) super.getCell();
    }

    @Override
    public void configureElementId(String elementIdPrefix, String columnId) {
        getCell().setElementIdPrefix(elementIdPrefix);
        getCell().setColumnId(columnId);
    }

}
