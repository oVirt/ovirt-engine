package org.ovirt.engine.ui.common.widget.action;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;

/**
 * Default action button widget implementation.
 */
public class SimpleActionButton extends WidgetTooltip implements ActionButton {

    Button button;

    public SimpleActionButton() {
        button = new Button();
        setWidget(button);
    }

    @Override
    public void setTooltip(SafeHtml toolTipText) {
        setHtml(toolTipText);
    }

    @Override
    public void setTooltip(SafeHtml toolTipText, Placement placement) {
        setTooltip(toolTipText);
        setPlacement(placement);
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
    public void setText(String label) {
        button.setText(label);
    }

    public void setIcon(IconType icon) {
        button.setIcon(icon);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        button.fireEvent(event);
    }

    protected Button asButton() {
        return button;
    }
}
