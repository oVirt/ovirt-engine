package org.ovirt.engine.ui.webadmin.widget.label;

import org.ovirt.engine.ui.common.widget.label.TextBoxLabelBase;
import org.ovirt.engine.ui.common.widget.renderer.NullableNumberRenderer;

import com.google.gwt.i18n.client.NumberFormat;

public class NullableNumberTextBoxLabel<T extends Number> extends TextBoxLabelBase<T> {

    public NullableNumberTextBoxLabel() {
        super(new NullableNumberRenderer());
    }

    public NullableNumberTextBoxLabel(String nullLabel) {
        super(new NullableNumberRenderer(nullLabel));
    }

    public NullableNumberTextBoxLabel(NumberFormat format) {
        super(new NullableNumberRenderer(format));
    }

}
