package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.ImageWithDecorator;
import org.ovirt.engine.ui.common.widget.table.cell.DecoratedImageResourceCell;
import org.ovirt.engine.ui.uicompat.EnumTranslator;

import com.google.gwt.user.cellview.client.Column;

public abstract class AbstractDecoratedImageColumn<T> extends Column<T, ImageWithDecorator> {

    public AbstractDecoratedImageColumn() {
        super(new DecoratedImageResourceCell());
    }

    @Override
    public DecoratedImageResourceCell getCell() {
        return (DecoratedImageResourceCell) super.getCell();
    }

    public void setTitle(String title) {
        getCell().setTitle(title);
    }

    public void setEnumTitle(Enum<?> enumObj) {
        setTitle(EnumTranslator.getInstance().translate(enumObj));
    }

}
