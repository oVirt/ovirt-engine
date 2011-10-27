package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.webadmin.widget.renderer.ObjectNameRenderer;

public abstract class ObjectNameColumn<T> extends RenderedTextColumn<T, Object[]> {

    public ObjectNameColumn() {
        super(new ObjectNameRenderer());
    }
}
