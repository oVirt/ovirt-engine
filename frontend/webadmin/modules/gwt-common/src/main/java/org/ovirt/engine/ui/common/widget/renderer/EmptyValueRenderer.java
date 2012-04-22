package org.ovirt.engine.ui.common.widget.renderer;

import com.google.gwt.text.shared.AbstractRenderer;

public class EmptyValueRenderer<T> extends AbstractRenderer<T> {

    private final String unAvailablePropertyLabel;

    public EmptyValueRenderer() {
        this(""); //$NON-NLS-1$
    }

    public EmptyValueRenderer(String unAvailablePropertyLabel) {
        this.unAvailablePropertyLabel = unAvailablePropertyLabel;
    }

    @Override
    public String render(T value) {
        return value != null && !value.equals("") ? value.toString() : unAvailablePropertyLabel; //$NON-NLS-1$
    }

}
