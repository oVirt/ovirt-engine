package org.ovirt.engine.ui.common.widget.label;

import org.ovirt.engine.ui.common.widget.renderer.BooleanRenderer;

import com.google.gwt.user.client.ui.ValueLabel;

public class BooleanTextBoxLabel extends ValueLabel<Boolean> {

    public BooleanTextBoxLabel() {
        super(new BooleanRenderer());
    }

    public BooleanTextBoxLabel(String trueText, String falseText) {
        super(new BooleanRenderer(trueText, falseText));
    }
}
