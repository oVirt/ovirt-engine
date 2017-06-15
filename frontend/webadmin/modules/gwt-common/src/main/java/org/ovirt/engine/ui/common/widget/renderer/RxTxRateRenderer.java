package org.ovirt.engine.ui.common.widget.renderer;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;

import com.google.gwt.text.shared.AbstractRenderer;

/**
 * Renderer for Rx/Tx transfer rates.
 */
public class RxTxRateRenderer extends AbstractRenderer<Double[]> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    private static final String NO_VALUE = constants.unAvailablePropertyLabel();
    private static final String ZERO_VALUE = "0"; //$NON-NLS-1$
    private static final String SMALL_VALUE = "< 1"; //$NON-NLS-1$

    @Override
    public String render(Double[] values) {
        if (values.length != 2 || values[0] == null || values[1] == null) {
            return NO_VALUE;
        }

        double x_rate = values[0];
        double speed = values[1];

        double calc = x_rate * speed / 100;

        if (calc < 1 && calc >= 0) {
            return SMALL_VALUE;
        } else if (calc > 0) {
            int retVal = (int) calc;
            return Integer.toString(retVal);
        }
        return ZERO_VALUE;
    }

    public static boolean isEmpty(String text) {
        return NO_VALUE.equals(text);
    }

    public static boolean isZero(String text) {
        return ZERO_VALUE.equals(text);
    }

    public static boolean isSmall(String text) {
        return SMALL_VALUE.equals(text);
    }

}
