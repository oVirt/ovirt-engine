package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.common.widget.renderer.UptimeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.AbstractRenderedTextColumn;

/**
 * Column for displaying uptime values.
 *
 * @param <T>
 *            Table row data type.
 */
public abstract class AbstractUptimeColumn<T> extends AbstractRenderedTextColumn<T, Double> {

    public AbstractUptimeColumn() {
        super(new UptimeRenderer());
    }

}
