package org.ovirt.engine.ui.webadmin.widget.label;

import org.ovirt.engine.ui.webadmin.widget.renderer.EmptyValueRenderer;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.TextBox;

public class TextBoxLabel extends TextBox {

    boolean handleEmptyValue;

    public TextBoxLabel() {
        setReadOnly(true);
        getElement().getStyle().setBorderWidth(0, Unit.PX);
        getElement().getStyle().setWidth(100, Unit.PCT);
    }

    public TextBoxLabel(String text) {
        super();
        setText(text);
    }

    public TextBoxLabel(boolean handleEmptyValue) {
        this();
        this.handleEmptyValue = handleEmptyValue;
    }

    @Override
    public void setText(String text) {
        String renderedText = new EmptyValueRenderer<String>(handleEmptyValue).render(text);
        renderedText = unEscapeRenderedText(renderedText);
        super.setText(renderedText);
        super.setTitle(renderedText);
    }

    private String unEscapeRenderedText(String renderedText) {
        renderedText = renderedText.replace("&lt;", "<");
        return renderedText;
    }
}
