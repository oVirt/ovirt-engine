package org.ovirt.engine.ui.webadmin.widget.label;

import org.ovirt.engine.ui.common.widget.renderer.NullableNumberRenderer;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.ValueLabel;

public class NullableNumberValueLabel<T extends Number> extends ValueLabel<T> {

    public NullableNumberValueLabel() {
        super(new NullableNumberRenderer());
    }

    public NullableNumberValueLabel(String nullLabel) {
        super(new NullableNumberRenderer(nullLabel));
    }

    public NullableNumberValueLabel(NumberFormat format) {
        super(new NullableNumberRenderer(format));
    }

}
