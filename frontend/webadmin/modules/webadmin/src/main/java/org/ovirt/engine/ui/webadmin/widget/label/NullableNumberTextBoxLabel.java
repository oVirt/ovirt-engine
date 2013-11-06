package org.ovirt.engine.ui.webadmin.widget.label;

import org.ovirt.engine.ui.common.widget.label.TextBoxLabelBase;
import org.ovirt.engine.ui.webadmin.widget.renderer.NullableNumberRenderer;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.text.shared.Renderer;

public class NullableNumberTextBoxLabel<T extends Number> extends TextBoxLabelBase<T> {
    public NullableNumberTextBoxLabel() {
        super((Renderer<T>) new NullableNumberRenderer());
    }

    public NullableNumberTextBoxLabel(String nullLabel) {
        super((Renderer<T>) new NullableNumberRenderer(nullLabel));
    }

    public NullableNumberTextBoxLabel(NumberFormat format) {
        super((Renderer<T>) new NullableNumberRenderer(format));
    }
}
