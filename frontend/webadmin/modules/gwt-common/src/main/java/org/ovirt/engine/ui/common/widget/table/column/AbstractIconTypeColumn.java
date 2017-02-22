package org.ovirt.engine.ui.common.widget.table.column;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.ui.common.widget.table.cell.IconTypeCell;

public abstract class AbstractIconTypeColumn<T> extends AbstractColumn<T, IconType> {

    public AbstractIconTypeColumn() {
        super(new IconTypeCell());
    }

    public AbstractIconTypeColumn(IconTypeCell cell) {
        super(cell);
    }

    @Override
    public IconTypeCell getCell() {
        return (IconTypeCell) super.getCell();
    }

    public IconType getDefaultImage() {
        return null;
    }
}
