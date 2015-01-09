package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.column.AbstractRenderedTextColumn;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.widget.renderer.RebalanceFileSizeRenderer;

public abstract class AbstractRebalanceFileSizeColumn<T> extends AbstractRenderedTextColumn<T, Long> {
    public AbstractRebalanceFileSizeColumn(ApplicationMessages messages) {
        super(new RebalanceFileSizeRenderer<Long>(messages));
    }
}
