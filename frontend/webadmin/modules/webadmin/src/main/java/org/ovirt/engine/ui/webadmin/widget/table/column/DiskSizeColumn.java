package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.webadmin.widget.renderer.DiskSizeRenderer;

public abstract class DiskSizeColumn<T> extends RenderedTextColumn<T, Long> {

    public DiskSizeColumn() {
        super(new DiskSizeRenderer());
    }

}
