package org.ovirt.engine.ui.common.widget;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RadioButton;


public class RadioButtonsHorizontalPanel extends HorizontalPanel {
    private String name;

    @UiConstructor
    public RadioButtonsHorizontalPanel(String name) {
        setStyleName("avw_contentWidgetContainer_pfly_fix"); //$NON-NLS-1$
        this.name = name;
    }

    public void addRadioButton(String text, boolean value, boolean enabled, ClickHandler clickHandler) {
        RadioButton radioButton = new RadioButton(name);
        radioButton.setText(text);
        radioButton.setValue(value);
        radioButton.setEnabled(enabled);
        radioButton.addClickHandler(clickHandler);
        radioButton.getElement().getStyle().setMarginLeft(10, Unit.PX);
        radioButton.getElement().getStyle().setMarginRight(20, Unit.PX);
        add(radioButton);
    }
}
