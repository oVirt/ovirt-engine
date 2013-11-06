package org.ovirt.engine.ui.common.widget.label;

import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;

public class EnumTextBoxLabel<E extends Enum<E>> extends TextBoxLabelBase<E> {

    public EnumTextBoxLabel() {
        super(new EnumRenderer<E>());
    }

}
