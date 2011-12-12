package org.ovirt.engine.ui.webadmin.widget.label;

import org.ovirt.engine.ui.webadmin.widget.renderer.MemorySizeRenderer;

import com.google.gwt.user.client.ui.ValueLabel;

public class MemorySizeLabel<T extends Number> extends ValueLabel<T> {

    public MemorySizeLabel() {
        super(new MemorySizeRenderer<T>());
    }

}
