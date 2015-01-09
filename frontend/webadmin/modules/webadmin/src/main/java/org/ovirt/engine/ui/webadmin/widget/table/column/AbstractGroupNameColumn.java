package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.column.AbstractRenderedTextColumn;
import org.ovirt.engine.ui.webadmin.widget.renderer.GroupNameRenderer;

public abstract class AbstractGroupNameColumn<T> extends AbstractRenderedTextColumn<T, Object[]> {

    public AbstractGroupNameColumn() {
        super(new GroupNameRenderer());
    }
}
