package org.ovirt.engine.ui.common.widget.table.column;

import java.util.Date;

import org.ovirt.engine.ui.common.widget.renderer.FullDateTimeRenderer;

/**
 * Column for displaying Date values using {@link FullDateTimeRenderer}.
 *
 * @param <T> Table row data type.
 */
public abstract class AbstractFullDateTimeColumn<T> extends AbstractRenderedTextColumn<T, Date> {

    /**
     * Use standard date format pattern of 'yyyy-MMM-dddd'.
     *
     * (Uses only 'MM' for month is locale is set to Japanese.)
     */
    public AbstractFullDateTimeColumn() {
        super(new FullDateTimeRenderer());
    }

    /**
     * Use date format pattern of 'yyyy-MMM-dddd'. Pass 'true' for includeTime if
     * you want to include the hours and minutes in the date ('yyyy-MMM-dddd HH:mm').
     * Pass 'true' for includeSeconds ('yyyy-MMM-dddd HH:mm:ss') if you want the seconds in the
     * date as well.
     *
     * (Uses only 'MM' for month is locale is set to Japanese.)
     */
    public AbstractFullDateTimeColumn(boolean includeTime, boolean includeSeconds) {
        super(new FullDateTimeRenderer(includeTime, includeSeconds));
    }

}
