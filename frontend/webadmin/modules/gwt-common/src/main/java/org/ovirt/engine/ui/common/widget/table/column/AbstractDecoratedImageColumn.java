package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.ImageWithDecorator;
import org.ovirt.engine.ui.common.widget.table.cell.DecoratedImageResourceCell;

public abstract class AbstractDecoratedImageColumn<T> extends AbstractColumn<T, ImageWithDecorator> {

    public AbstractDecoratedImageColumn() {
        super(new DecoratedImageResourceCell());
    }

    @Override
    public DecoratedImageResourceCell getCell() {
        return (DecoratedImageResourceCell) super.getCell();
    }

}
