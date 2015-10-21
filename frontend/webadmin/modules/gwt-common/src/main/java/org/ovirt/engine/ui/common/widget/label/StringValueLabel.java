package org.ovirt.engine.ui.common.widget.label;

import org.ovirt.engine.ui.common.widget.renderer.EmptyValueRenderer;

import com.google.gwt.user.client.ui.ValueLabel;

public class StringValueLabel extends ValueLabel<String> {

    public StringValueLabel() {
        super(new EmptyValueRenderer<String>());
    }

    public StringValueLabel(String text) {
        this();
        setValue(text);
    }

    /**
     * Overridden to return "" from an empty text box.
     *
     * @see com.google.gwt.user.client.ui.TextBoxBase#getValue()
     */
    @Override
    public String getValue() {
        String raw = super.getValue();
        return raw == null ? "" : raw; //$NON-NLS-1$
    }
}
