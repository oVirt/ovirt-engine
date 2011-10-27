package org.ovirt.engine.ui.webadmin.widget.label;

import org.ovirt.engine.ui.webadmin.widget.renderer.MemorySizeRenderer;

import com.google.gwt.user.client.ui.ValueLabel;

public class MemorySizeLabel<T extends Number> extends ValueLabel<T> {

    T value;

    public MemorySizeLabel() {
        super(new MemorySizeRenderer<T>());
    }

    @Override
    public String toString() {
        return new MemorySizeRenderer<T>().render(value);
    }

    @Override
    public void setValue(T value) {
        super.setValue(value);

        this.value = value;
    }

}
