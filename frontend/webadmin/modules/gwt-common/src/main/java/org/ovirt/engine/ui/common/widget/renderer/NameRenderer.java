package org.ovirt.engine.ui.common.widget.renderer;

import org.ovirt.engine.core.common.businessentities.Nameable;

public class NameRenderer<E extends Nameable> extends NullSafeRenderer<E> {
    @Override
    protected String renderNullSafe(E object) {
        return object.getName();
    }
}
