package org.ovirt.engine.ui.common.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.ui.uicommonweb.ViewFilter;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

public class ViewRadioGroup<K> extends Composite {

    private final List<? extends ViewFilter<K>> items;
    private final Map<K, RadioButton> buttons = new HashMap<>();

    public ViewRadioGroup(List<? extends ViewFilter<K>> items) {
        this.items = items;
        initWidget(getRadioGroupPanel());
    }

    private Widget getRadioGroupPanel() {
        FlowPanel buttonsPanel = new FlowPanel();
        buttonsPanel.getElement().getStyle().setProperty("marginLeft", "auto"); //$NON-NLS-1$ //$NON-NLS-2$
        buttonsPanel.getElement().getStyle().setProperty("marginRight", "auto"); //$NON-NLS-1$ //$NON-NLS-2$

        for (ViewFilter<K> item : items) {
            RadioButton radioButton = new RadioButton("viewRadioGroup", item.toString()); //$NON-NLS-1$
            radioButton.getElement().getStyle().setMarginRight(20, Unit.PX);
            radioButton.setText(item.getText());
            buttons.put(item.getValue(), radioButton);
            buttonsPanel.add(radioButton);
        }

        setSelectedValue(items.get(0).getValue());

        return buttonsPanel;
    }

    public void addClickHandler(ClickHandler clickHandler) {
        for (RadioButton button : buttons.values()) {
            button.addClickHandler(clickHandler);
        }
    }

    public RadioButton getButton(String name) {
        return buttons.get(name);
    }

    public K getSelectedValue() {
        for (Map.Entry<K, RadioButton> buttonEntry : buttons.entrySet()) {
            if (buttonEntry.getValue().getValue()) {
                return buttonEntry.getKey();
            }
        }
        return null;
    }

    public void setSelectedValue(K value) {
        RadioButton button = buttons.get(value);

        if (button != null) {
            // Reset old selected radio
            if (buttons.get(getSelectedValue()) != null) {
                buttons.get(getSelectedValue()).setValue(false);
            }

            // Initialize new selected radio
            button.setValue(true);
        }
    }
}
