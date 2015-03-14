package org.ovirt.engine.ui.common.widget.label;

import org.ovirt.engine.ui.common.widget.renderer.MemorySizeRenderer;

public class MemorySizeTextBoxLabel<T extends Number> extends TextBoxLabelBase<T> {

    public MemorySizeTextBoxLabel() {
        super(new MemorySizeRenderer<T>());
    }

}
