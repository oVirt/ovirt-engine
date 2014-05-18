package org.ovirt.engine.ui.common.widget.table.column;

import com.google.gwt.user.cellview.client.Column;
import org.ovirt.engine.ui.uicompat.EnumTranslator;

public abstract class ImageWithDecoratorColumn<T> extends Column<T, ImageWithDecorator> {

    public ImageWithDecoratorColumn() {
        super(new DecoratedImageResouceCell());
    }

    @Override
    public DecoratedImageResouceCell getCell() {
        return (DecoratedImageResouceCell) super.getCell();
    }

    public void setTitle(String title) {
        getCell().setTitle(title);
    }

    public void setEnumTitle(Enum<?> enumObj) {
        setTitle(EnumTranslator.createAndTranslate(enumObj));
    }

}
