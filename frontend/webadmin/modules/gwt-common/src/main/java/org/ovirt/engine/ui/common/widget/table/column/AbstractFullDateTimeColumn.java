package org.ovirt.engine.ui.common.widget.table.column;

import java.util.Comparator;
import java.util.Date;

import org.ovirt.engine.ui.common.widget.renderer.FullDateTimeRenderer;

/**
 * Column for displaying Date values using {@link FullDateTimeRenderer}.
 *
 * @param <T> Table row data type.
 */
public abstract class AbstractFullDateTimeColumn<T> extends AbstractRenderedTextColumn<T, Date> {

    /**
     * Create a new date column displaying the Date in the predefined DATE_TIME_MEDIUM format for
     * the current selected locale. This includes the time part including seconds.
     * @see DateTimeFormat.PredefinedFormat
     */
    public AbstractFullDateTimeColumn() {
        super(new FullDateTimeRenderer());
    }

    /**
     * Create a new date column displaying the Date in a predefined format for
     * the current selected locale. If you pass in true the format will be DATE_TIME_MEDIUM, if you pass in
     * false the format will be DATE_MEDIUM. Note DATE_TIME_MEDIUM includes seconds in the result. Also note there
     * is NO valid predefined format that includes the full year and does NOT include the seconds.
     * @see DateTimeFormat.PredefinedFormat
     */
    public AbstractFullDateTimeColumn(boolean includeTime) {
        super(new FullDateTimeRenderer(includeTime));
    }

    /**
     * Enables default <em>client-side</em> sorting for this column, by the Date Time ordering of the displayed text.
     */
    @Override
    public void makeSortable() {
        makeSortable(Comparator.nullsFirst(Comparator.comparing(this::getRawValue)));
    }
}
