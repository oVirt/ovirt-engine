package org.ovirt.engine.ui.common.widget.label;

import org.ovirt.engine.ui.common.widget.renderer.MemorySizeRenderer;

import com.google.gwt.user.client.ui.ValueLabel;

public class MemorySizeTextBoxLabel<T extends Number> extends ValueLabel<T> {

    public MemorySizeTextBoxLabel() {
        super(new MemorySizeRenderer<T>());
    }

}
