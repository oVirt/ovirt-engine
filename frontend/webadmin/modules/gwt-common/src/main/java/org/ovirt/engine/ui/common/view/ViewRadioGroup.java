package org.ovirt.engine.ui.common.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.RadioButton;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.ovirt.engine.ui.uicommonweb.ViewFilter;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class ViewRadioGroup<K> extends Composite {

    private static final String GROUP_NAME = "viewRadioGroup"; //$NON-NLS-1$

    public interface ViewRadioGroupChangeHandler<K> {
        void selectionChanged(K newSelection);
    }

    private final List<ViewRadioGroupChangeHandler<K>> changeHandlers = new ArrayList<>();
    private final List<? extends ViewFilter<K>> items;
    private final Map<K, RadioButton> buttons = new HashMap<>();

    public ViewRadioGroup(List<? extends ViewFilter<K>> items) {
        this.items = items;
        initWidget(getRadioGroupPanel());
    }

    private Widget getRadioGroupPanel() {
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.setDataToggle(Toggle.BUTTONS);

        for (ViewFilter<K> item : items) {
            RadioButton radioButton = new RadioButton(GROUP_NAME);
            radioButton.setText(item.getText());
            radioButton.addClickHandler(event -> fireChangeHandlers(item.getValue()));

            buttons.put(item.getValue(), radioButton);
            buttonGroup.add(radioButton);
        }

        setSelectedValue(items.get(0).getValue());

        return buttonGroup;
    }

    public void addChangeHandler(ViewRadioGroupChangeHandler<K> handler) {
        if (!changeHandlers.contains(handler)) {
            changeHandlers.add(handler);
        }
    }

    private void fireChangeHandlers(K newItem) {
        for (ViewRadioGroupChangeHandler<K> viewRadioGroupChangeHandler : changeHandlers) {
            viewRadioGroupChangeHandler.selectionChanged(newItem);
        }
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
        // Reset old selected radio
        K selected = getSelectedValue();
        if (selected != null && buttons.get(selected) != null) {
            buttons.get(selected).setValue(false);
            buttons.get(selected).setActive(false);
        }

        // Initialize new selected radio
        if (value != null && buttons.get(value) != null) {
            buttons.get(value).setValue(true);
            buttons.get(value).setActive(true);
        }
    }
}
