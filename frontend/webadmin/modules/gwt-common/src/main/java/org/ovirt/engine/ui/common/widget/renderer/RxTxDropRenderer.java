package org.ovirt.engine.ui.common.widget.renderer;

import java.math.BigInteger;

import com.google.gwt.text.shared.AbstractRenderer;

/**
 * Renderer for Rx/Tx drop rates.
 */
public class RxTxDropRenderer extends AbstractRenderer<BigInteger[]> {

    private static final String ZERO_VALUE = "0"; //$NON-NLS-1$

    @Override
    public String render(BigInteger[] values) {
        if (values.length != 2 || values[0] == null && values[1] == null) {
            return ZERO_VALUE;
        } else if (values[0] == null) {
            return String.valueOf(values[1]);
        } else if (values[1] == null) {
            return String.valueOf(values[0]);
        } else {
            return String.valueOf(values[0].add(values[1]));
        }
    }
}
