package org.ovirt.engine.ui.webadmin.widget.table;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PushButton;

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
    PushButton button;

    @Override
    public void setEnabledHtml(SafeHtml html) {
        button.getUpFace().setHTML(html);
    }

    @Override
    public void setDisabledHtml(SafeHtml html) {
        button.getUpDisabledFace().setHTML(html);
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
    public void setTitle(String title) {
        button.setTitle(title);
    }
}
