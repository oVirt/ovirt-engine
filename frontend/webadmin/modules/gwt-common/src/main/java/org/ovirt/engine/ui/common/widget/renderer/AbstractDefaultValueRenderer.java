package org.ovirt.engine.ui.common.widget.renderer;

import com.google.gwt.text.shared.AbstractRenderer;

/**
 * Renderer that uses an existing renderer for all non null values but shows predefined text
 * and provided default value for null.
 *
 */
public abstract class AbstractDefaultValueRenderer<T> extends AbstractRenderer<T> {

    private AbstractRenderer<T> valueRenderer;

    private T defaultValue;

    public AbstractDefaultValueRenderer(AbstractRenderer<T> renderer) {
        this.valueRenderer = renderer;
    }

    protected abstract String getDefaultValueLabel();

    @Override
    public String render(T item) {
        if (item != null) {
            return valueRenderer.render(item);
        }

        if (defaultValue != null) {
            String valueString = valueRenderer.render(defaultValue);
            return renderDefaultValue(valueString);
        }
        return renderDefaultValue();
    }

    private String renderDefaultValue() {
        return getDefaultValueLabel();
    }

    private String renderDefaultValue(String valueString) {
        return getDefaultValueLabel() + " (" + valueString + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
    }
}
