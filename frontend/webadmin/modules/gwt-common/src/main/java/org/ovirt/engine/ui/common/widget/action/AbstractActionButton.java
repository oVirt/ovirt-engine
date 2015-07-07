package org.ovirt.engine.ui.common.widget.action;

import org.gwtbootstrap3.client.ui.constants.Placement;
import org.ovirt.engine.ui.common.widget.tooltip.TooltipMixin;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ToggleButton;

/**
 * Base class used to implement action button widgets.
 * <p>
 * Subclasses are free to style the UI, given that they declare:
 * <ul>
 * <li>{@link #button} widget representing the action button
 * </ul>
 */
public abstract class AbstractActionButton extends Composite implements ActionButton {

    @UiField
    public ToggleButton button;

    @Override
    public void setEnabledHtml(SafeHtml html) {
        button.getUpFace().setHTML(html);
    }

    @Override
    public void setDisabledHtml(SafeHtml html) {
        button.getUpDisabledFace().setHTML(html);
        button.getDownDisabledFace().setHTML(html);
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return button.addClickHandler(handler);
    }

    @Override
    public boolean isEnabled() {
        return button.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        button.setEnabled(enabled);
    }

    @Override
    public void setTooltip(SafeHtml tooltip) {
        TooltipMixin.addTooltipToElement(tooltip, getElement());
    }

    @Override
    public void setTooltip(SafeHtml tooltip, Placement placement) {
        TooltipMixin.addTooltipToElement(tooltip, getElement(), placement);
    }

    @Override
    public ToggleButton asToggleButton() {
        return button;
    }

}
