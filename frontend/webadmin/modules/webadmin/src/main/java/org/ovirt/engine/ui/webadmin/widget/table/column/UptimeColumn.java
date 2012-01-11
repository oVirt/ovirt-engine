package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.column.RenderedTextColumn;
import org.ovirt.engine.ui.webadmin.widget.renderer.UptimeRenderer;

/**
 * Column for displaying uptime values.
 *
 * @param <T>
 *            Table row data type.
 */
public abstract class UptimeColumn<T> extends RenderedTextColumn<T, Double> {

    public UptimeColumn() {
        super(new UptimeRenderer());
    }

}
