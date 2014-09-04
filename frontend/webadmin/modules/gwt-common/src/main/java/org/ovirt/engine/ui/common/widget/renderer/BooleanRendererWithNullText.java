package org.ovirt.engine.ui.common.widget.renderer;

public class BooleanRendererWithNullText extends BooleanRenderer {
    private final String nullText;

    public BooleanRendererWithNullText(String trueText, String falseText, String nullText) {
        super(trueText, falseText);
        this.nullText = nullText;
    }

    @Override
    public String render(Boolean valueToRender) {
        if (valueToRender == null) {
            return nullText;
        }

        return super.render(valueToRender);
    }
}
