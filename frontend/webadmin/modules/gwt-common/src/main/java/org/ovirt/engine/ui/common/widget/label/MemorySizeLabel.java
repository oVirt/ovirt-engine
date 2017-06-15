package org.ovirt.engine.ui.common.widget.label;

import org.ovirt.engine.ui.common.widget.renderer.MemorySizeRenderer;

import com.google.gwt.user.client.ui.ValueLabel;

public class MemorySizeLabel<T extends Number> extends ValueLabel<T> {

    public MemorySizeLabel() {
        super(new MemorySizeRenderer<T>());
    }

}
