package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.column.RenderedTextColumn;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.widget.renderer.RebalanceFileSizeRenderer;

public abstract class RebalanceFileSizeColumn<T> extends RenderedTextColumn<T, Long> {
    public RebalanceFileSizeColumn(ApplicationMessages messages) {
        super(new RebalanceFileSizeRenderer<Long>(messages));
    }
}
