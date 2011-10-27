package org.ovirt.engine.ui.webadmin.widget.label;

import org.ovirt.engine.ui.webadmin.widget.renderer.EmptyValueRenderer;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.TextBox;

public class TextBoxLabel extends TextBox {

    public TextBoxLabel() {
        setReadOnly(true);
        getElement().getStyle().setBorderWidth(0, Unit.PX);
    }

    @Override
    public void setText(String text) {
        super.setText(new EmptyValueRenderer<String>().render(text));
    }
}
