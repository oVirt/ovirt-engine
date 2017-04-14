package org.ovirt.engine.ui.common.widget.table.column;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.ui.common.widget.renderer.RxTxRateRenderer;

/**
 * Column for displaying Rx/Tx transfer rates.
 *
 * @param <T>
 *            Table row data type.
 */
public abstract class AbstractRxTxRateColumn<T> extends AbstractRenderedTextColumn<T, Double[]> {

    public AbstractRxTxRateColumn() {
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

    @Override
    public void makeSortable() {
        makeSortable(Comparator.comparing((T t) -> !RxTxRateRenderer.isEmpty(getValue(t)))
                .thenComparing(t -> !RxTxRateRenderer.isZero(getValue(t)))
                .thenComparing(t -> !RxTxRateRenderer.isSmall(getValue(t)))
                .thenComparing(this::getValue, new LexoNumericComparator()));
    }
}
