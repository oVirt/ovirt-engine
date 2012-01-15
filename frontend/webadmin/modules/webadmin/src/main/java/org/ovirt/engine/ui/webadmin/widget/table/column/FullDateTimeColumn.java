package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.Date;

import org.ovirt.engine.ui.webadmin.widget.renderer.FullDateTimeRenderer;

/**
 * Column for displaying Date values using {@link FullDateTimeRenderer}.
 *
 * @param <T>
 *            Table row data type.
 */
public abstract class FullDateTimeColumn<T> extends RenderedTextColumn<T, Date> {

    public FullDateTimeColumn() {
        super(new FullDateTimeRenderer());
    }

}
