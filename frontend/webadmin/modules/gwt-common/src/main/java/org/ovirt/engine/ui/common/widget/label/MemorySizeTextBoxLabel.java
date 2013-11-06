package org.ovirt.engine.ui.common.widget.label;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.widget.renderer.MemorySizeRenderer;

public class MemorySizeTextBoxLabel<T extends Number> extends TextBoxLabelBase<T> {

    public MemorySizeTextBoxLabel(CommonApplicationConstants constants) {
        super(new MemorySizeRenderer<T>(constants));
    }

}
