package org.ovirt.engine.ui.common.widget;

import java.util.List;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
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
        applyCommonValidationStyles();
        markAsValid();
    }

    @Override
    public void markAsValid() {
        valid = true;
        applyCommonValidationStyles();
        getValidatedWidgetStyle().setBorderColor("gray"); //$NON-NLS-1$
    }

    @Override
    public void markAsInvalid(List<String> validationHints) {
        valid = false;
        applyCommonValidationStyles();
        getValidatedWidgetStyle().setBorderColor("red"); //$NON-NLS-1$
    }

    protected void applyCommonValidationStyles() {
        getValidatedWidgetStyle().setBorderWidth(1, Unit.PX);
        getValidatedWidgetStyle().setBorderStyle(BorderStyle.SOLID);
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
