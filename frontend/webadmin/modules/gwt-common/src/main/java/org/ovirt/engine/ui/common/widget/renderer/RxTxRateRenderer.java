package org.ovirt.engine.ui.common.widget.renderer;

import com.google.gwt.text.shared.AbstractRenderer;

/**
 * Renderer for Rx/Tx transfer rates.
 */
public class RxTxRateRenderer extends AbstractRenderer<Double[]> {

    @Override
    public String render(Double[] values) {
        if (values.length != 2 || values[0] == null || values[1] == null) {
            return "[N/A]"; //$NON-NLS-1$
        }

        double x_rate = values[0];
        double speed = values[1];

        double calc = x_rate * speed / 100;

        if (calc < 1 && calc >= 0) {
            return "< 1"; //$NON-NLS-1$
        } else if (calc > 0) {
            int retVal = (int) calc;
            return Integer.toString(retVal);
        }
        return "0"; //$NON-NLS-1$
    }

}
