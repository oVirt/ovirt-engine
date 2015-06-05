package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.column.ColumnWithElementId;

import com.google.gwt.user.cellview.client.Column;

public abstract class GlusterCapacityColumn<P, Q> extends Column<P, Q> implements ColumnWithElementId {

    public GlusterCapacityColumn(GlusterCapacityCell<Q> cell) {
        super(cell);
    }

    @Override
    public void configureElementId(String elementIdPrefix, String columnId) {
        getCell().setElementIdPrefix(elementIdPrefix);
        getCell().setColumnId(columnId);
    }

    @Override
    public GlusterCapacityCell<Q> getCell() {
        return (GlusterCapacityCell<Q>) super.getCell();
    }
}
