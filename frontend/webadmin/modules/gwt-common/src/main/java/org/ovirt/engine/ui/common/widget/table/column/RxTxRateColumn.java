package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.renderer.RxTxRateRenderer;

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
        Double rate = object != null ? getRate(object) : null;
        Double speed = object != null ? getSpeed(object) : null;
        return new Double[] { rate, speed };
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
