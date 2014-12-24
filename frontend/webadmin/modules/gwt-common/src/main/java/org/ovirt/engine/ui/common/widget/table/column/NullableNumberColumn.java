package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.renderer.NullableNumberRenderer;

public abstract class NullableNumberColumn<T> extends RenderedTextColumn<T, Number> {

    public NullableNumberColumn() {
        super(new NullableNumberRenderer());
    }

}
