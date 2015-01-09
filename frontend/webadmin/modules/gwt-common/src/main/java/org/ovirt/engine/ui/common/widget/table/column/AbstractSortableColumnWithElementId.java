package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.cell.CellWithElementId;

/**
 * A {@link AbstractSortableColumn} that implements ColumnWithElementId (has an id).
 *
 * @param <T>
 *            Table row data type.
 * @param <C>
 *            Cell data type.
 */
public abstract class AbstractSortableColumnWithElementId<T, C> extends AbstractSortableColumn<T, C> implements ColumnWithElementId {

    public AbstractSortableColumnWithElementId(CellWithElementId<C> cell) {
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
