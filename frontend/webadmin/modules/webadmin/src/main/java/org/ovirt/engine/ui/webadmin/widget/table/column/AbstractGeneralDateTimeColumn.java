package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.Date;

import org.ovirt.engine.ui.common.widget.table.column.AbstractRenderedTextColumn;
import org.ovirt.engine.ui.webadmin.widget.renderer.GeneralDateTimeRenderer;

/**
 * Column for displaying Date values using {@link GeneralDateTimeRenderer}.
 *
 * @param <T>
 *            Table row data type.
 */
public abstract class AbstractGeneralDateTimeColumn<T> extends AbstractRenderedTextColumn<T, Date> {

    public AbstractGeneralDateTimeColumn() {
        super(new GeneralDateTimeRenderer());
    }

}
