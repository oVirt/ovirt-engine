package org.ovirt.engine.ui.common.widget.label;

import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;

import com.google.gwt.user.client.ui.ValueLabel;

public class EnumTextBoxLabel<E extends Enum<E>> extends ValueLabel<E> {

    public EnumTextBoxLabel() {
        super(new EnumRenderer<E>());
    }

}
