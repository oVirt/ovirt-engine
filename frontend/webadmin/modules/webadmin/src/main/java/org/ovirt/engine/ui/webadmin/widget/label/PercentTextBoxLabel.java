package org.ovirt.engine.ui.webadmin.widget.label;

import org.ovirt.engine.ui.common.widget.label.TextBoxLabelBase;
import org.ovirt.engine.ui.webadmin.widget.renderer.PercentRenderer;

public class PercentTextBoxLabel<T extends Number> extends TextBoxLabelBase<T> {

    public PercentTextBoxLabel() {
        super(new PercentRenderer<T>());
    }

}
