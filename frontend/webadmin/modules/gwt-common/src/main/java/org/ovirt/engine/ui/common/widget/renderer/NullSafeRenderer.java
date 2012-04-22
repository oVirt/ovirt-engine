package org.ovirt.engine.ui.common.widget.renderer;

import com.google.gwt.text.shared.AbstractRenderer;

public abstract class NullSafeRenderer<T> extends AbstractRenderer<T> {

    @Override
    public String render(T object) {
        return object == null ? "" : renderNullSafe(object); //$NON-NLS-1$
    }

    protected abstract String renderNullSafe(T object);

}
