package org.ovirt.engine.ui.common.widget.renderer;

import com.google.gwt.text.shared.AbstractRenderer;

public class BooleanRenderer extends AbstractRenderer<Boolean> {

    String trueText;
    String falseText;

    public BooleanRenderer() {
        super();
    }

    public BooleanRenderer(String trueText, String falseText) {
        this.trueText = trueText;
        this.falseText = falseText;
    }

    @Override
    public String render(Boolean valueToRender) {
        if (valueToRender == null) {
            return "";
        }

        if (trueText == null || falseText == null) {
            return valueToRender.toString();
        }

        return valueToRender.booleanValue() ? trueText : falseText;
    }

}
