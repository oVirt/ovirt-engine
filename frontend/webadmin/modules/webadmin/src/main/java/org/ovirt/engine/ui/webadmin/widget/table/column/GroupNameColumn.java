package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.column.RenderedTextColumn;
import org.ovirt.engine.ui.webadmin.widget.renderer.GroupNameRenderer;

public abstract class GroupNameColumn<T> extends RenderedTextColumn<T, Object[]> {

    public GroupNameColumn() {
        super(new GroupNameRenderer());
    }
}
