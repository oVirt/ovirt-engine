package org.ovirt.engine.ui.webadmin.widget.label;

import org.ovirt.engine.ui.webadmin.widget.renderer.PercentRenderer;

import com.google.gwt.user.client.ui.ValueLabel;

public class PercentTextBoxLabel<T extends Number> extends ValueLabel<T> {

    public PercentTextBoxLabel() {
        super(new PercentRenderer<T>());
    }

}
