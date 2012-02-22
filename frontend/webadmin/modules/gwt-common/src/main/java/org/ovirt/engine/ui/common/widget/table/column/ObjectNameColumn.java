package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.renderer.ObjectNameRenderer;

public abstract class ObjectNameColumn<T> extends RenderedTextColumn<T, Object[]> {

    public ObjectNameColumn() {
        super(new ObjectNameRenderer());
    }
}
