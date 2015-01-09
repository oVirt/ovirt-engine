package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.renderer.ObjectNameRenderer;

public abstract class AbstractObjectNameColumn<T> extends AbstractRenderedTextColumn<T, Object[]> {

    public AbstractObjectNameColumn() {
        super(new ObjectNameRenderer());
    }
}
