package org.ovirt.engine.ui.webadmin.widget.renderer;

import com.google.gwt.text.shared.AbstractRenderer;

public class BooleanRenderer extends AbstractRenderer<Boolean> {

    String trueText;
    String falseText;

    public BooleanRenderer() {
        super();
    }

    public BooleanRenderer(String trueText, String falseText) {
        super();

        this.trueText = trueText;
        this.falseText = falseText;
    }

    @Override
    public String render(Boolean bool) {
        if (trueText == null || falseText == null) {
            return bool.toString();
        }

        return bool != null && bool.booleanValue() ? trueText : falseText;
    }
}
