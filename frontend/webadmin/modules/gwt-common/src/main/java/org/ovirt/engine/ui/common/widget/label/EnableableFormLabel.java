package org.ovirt.engine.ui.common.widget.label;

import org.gwtbootstrap3.client.ui.FormLabel;
import org.ovirt.engine.ui.common.css.OvirtCss;

import com.google.gwt.user.client.ui.HasEnabled;

public class EnableableFormLabel extends FormLabel implements HasEnabled {

    private boolean enabled;

    public EnableableFormLabel() {
    }

    public EnableableFormLabel(String text) {
        setText(text);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (enabled) {
            getElement().replaceClassName(OvirtCss.LABEL_DISABLED, OvirtCss.LABEL_ENABLED);
        } else {
            getElement().replaceClassName(OvirtCss.LABEL_ENABLED, OvirtCss.LABEL_DISABLED);
        }
    }
}
