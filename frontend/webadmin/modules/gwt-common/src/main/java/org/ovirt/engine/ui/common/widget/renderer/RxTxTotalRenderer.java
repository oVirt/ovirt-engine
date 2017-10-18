package org.ovirt.engine.ui.common.widget.renderer;

import com.google.gwt.i18n.client.NumberFormat;

public class RxTxTotalRenderer extends NullableNumberRenderer {
    public RxTxTotalRenderer() {
        super(NumberFormat.getDecimalFormat());
    }
}
