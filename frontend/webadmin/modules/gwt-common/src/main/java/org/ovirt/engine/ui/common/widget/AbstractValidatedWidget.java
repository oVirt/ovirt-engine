package org.ovirt.engine.ui.common.widget;

import java.util.List;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Convenience base class for widgets implementing the {@link HasValidation} interface.
 */
public abstract class AbstractValidatedWidget extends Composite implements HasValidation {

    private boolean valid = true;

    @Override
    protected void initWidget(Widget widget) {
        super.initWidget(widget);
        markAsValid();
    }

    @Override
    public void markAsValid() {
        valid = true;
        getValidatedWidgetStyle().setBorderColor("none"); //$NON-NLS-1$
    }

    @Override
    public void markAsInvalid(List<String> validationHints) {
        valid = false;
        getValidatedWidgetStyle().setBorderColor("red"); //$NON-NLS-1$
    }

    protected String getValidationTooltipText(List<String> validationHints) {
        return validationHints != null && validationHints.size() > 0 ? validationHints.get(0) : null;
    }

    protected Style getValidatedWidgetStyle() {
        return getValidatedWidget().getElement().getStyle();
    }

    protected abstract Widget getValidatedWidget();

    @Override
    public boolean isValid() {
        return valid;
    }
}
