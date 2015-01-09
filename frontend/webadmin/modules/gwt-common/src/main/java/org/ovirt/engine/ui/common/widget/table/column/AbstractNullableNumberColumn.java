package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.renderer.NullableNumberRenderer;

public abstract class AbstractNullableNumberColumn<T> extends AbstractRenderedTextColumn<T, Number> {

    public AbstractNullableNumberColumn() {
        super(new NullableNumberRenderer());
    }

}
