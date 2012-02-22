package org.ovirt.engine.ui.common.widget.label;

import org.ovirt.engine.ui.common.widget.renderer.EmptyValueRenderer;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.TextBox;

public class TextBoxLabel extends TextBox {

    private boolean handleEmptyValue = false;
    private String unAvailablePropertyLabel = "";

    public TextBoxLabel() {
        setReadOnly(true);
        getElement().getStyle().setBorderWidth(0, Unit.PX);
        getElement().getStyle().setWidth(100, Unit.PCT);
    }

    public TextBoxLabel(String text) {
        this();
        setText(text);
    }

    public TextBoxLabel(boolean handleEmptyValue, String unAvailablePropertyLabel) {
        this();
        this.handleEmptyValue = handleEmptyValue;
        this.unAvailablePropertyLabel = unAvailablePropertyLabel;
    }

    @Override
    public void setText(String text) {
        String renderedText = new EmptyValueRenderer<String>(
                handleEmptyValue ? unAvailablePropertyLabel : "").render(text);
        renderedText = unEscapeRenderedText(renderedText);
        super.setText(renderedText);
        setTitle(renderedText);
    }

    private String unEscapeRenderedText(String renderedText) {
        renderedText = renderedText.replace("&lt;", "<");
        return renderedText;
    }

}
