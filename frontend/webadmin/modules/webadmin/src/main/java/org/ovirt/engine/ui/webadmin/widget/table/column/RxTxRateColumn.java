package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.webadmin.widget.renderer.RxTxRateRenderer;

/**
 * Column for displaying Rx/Tx transfer rates.
 *
 * @param <T>
 *            Table row data type.
 */
public abstract class RxTxRateColumn<T> extends RenderedTextColumn<T, Double[]> {

    public RxTxRateColumn() {
        super(new RxTxRateRenderer());
    }

    @Override
    protected Double[] getRawValue(T object) {
        return new Double[] { getRate(object), getSpeed(object) };
    }

    /**
     * Returns the Rx/Tx transfer rate.
     */
    protected abstract Double getRate(T object);

    /**
     * Returns the transfer speed.
     */
    protected abstract Double getSpeed(T object);

}
