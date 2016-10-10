package org.ovirt.engine.ui.common.widget;

import java.util.List;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Convenience base class for widgets implementing the {@link HasValidation} interface.
 */
public abstract class AbstractValidatedWidget extends Composite implements HasValidation, PatternFlyCompatible {

    // Bootstrap error indicator class. Works in combination with form-control
    private static final String HAS_ERROR = "has-error"; // $NON-NLS-1$

    private boolean valid = true;

    //Store if we are using patternfly styles.
    protected boolean usePatternfly = false;

    @Override
    protected void initWidget(Widget widget) {
        super.initWidget(widget);
        markAsValid();
    }

    @Override
    public void markAsValid() {
        valid = true;
        if (usePatternfly) {
            removeStyleName(HAS_ERROR);
        } else {
            getValidatedWidgetStyle().setBorderColor("none"); //$NON-NLS-1$
        }
    }

    @Override
    public void markAsInvalid(List<String> validationHints) {
        valid = false;
        if (usePatternfly) {
            addStyleName(HAS_ERROR);
        } else {
            getValidatedWidgetStyle().setBorderColor("red"); //$NON-NLS-1$
        }
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

    public void setUsePatternFly(final boolean usePatternfly) {
        this.usePatternfly = usePatternfly;
    }
}
