package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.ui.uicompat.EventArgs;

public final class ValueEventArgs<T> extends EventArgs {

    private T value;

    public T getValue() {
        return value;
    }

    private void setValue(T value) {
        this.value = value;
    }

    public ValueEventArgs(T value) {
        setValue(value);
    }
}
