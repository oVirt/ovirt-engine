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
        makeSortable(new Comparator<T>() {

            private LexoNumericComparator lexoNumeric = new LexoNumericComparator();

            @Override
            public int compare(T o1, T o2) {
                String text1 = getValue(o1);
                String text2 = getValue(o2);
                if (text1.equals(text2)) {
                    return 0;
                } else if (RxTxRateRenderer.isEmpty(text1) || RxTxRateRenderer.isEmpty(text2)) {
                    return RxTxRateRenderer.isEmpty(text1) ? -1 : 1;
                } else if (RxTxRateRenderer.isZero(text1) || RxTxRateRenderer.isZero(text2)) {
                    return RxTxRateRenderer.isZero(text1) ? -1 : 1;
                } else if (RxTxRateRenderer.isSmall(text1) || RxTxRateRenderer.isSmall(text2)) {
                    return RxTxRateRenderer.isSmall(text1) ? -1 : 1;
                } else {
                    return lexoNumeric.compare(text1, text2);
                }
            }
        });
    }

}
