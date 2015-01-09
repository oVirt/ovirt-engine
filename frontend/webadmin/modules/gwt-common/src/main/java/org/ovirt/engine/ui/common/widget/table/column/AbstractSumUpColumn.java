package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.renderer.SumUpRenderer;

/**
 * Column for displaying summed up values.
 *
 * @param <T>
 *            Table row data type.
 */
public abstract class AbstractSumUpColumn<T> extends AbstractRenderedTextColumn<T, Double[]> {

    public AbstractSumUpColumn() {
        super(new SumUpRenderer());
    }

}
