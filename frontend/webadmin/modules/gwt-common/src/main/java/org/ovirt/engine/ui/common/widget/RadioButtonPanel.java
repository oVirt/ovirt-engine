package org.ovirt.engine.ui.common.widget;

import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.RadioButton;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiConstructor;

public class RadioButtonPanel extends ButtonGroup {
    private String name;

    @UiConstructor
    public RadioButtonPanel(String name) {
        this.name = name;
    }

    private RadioButton createRadioButton(String title, boolean active, boolean enabled, ValueChangeHandler<Boolean> handler) {
        RadioButton button = new RadioButton(title);
        button.setHTML(title);
        button.setName(name);
        button.setEnabled(enabled);
        button.setActive(active);
        button.addValueChangeHandler(handler);
        return button;
    }

    public void addRadioButton(String title, boolean active, boolean enabled,
            ValueChangeHandler<Boolean> handler) {
        add(createRadioButton(title, active, enabled, handler));
    }
}
