package org.ovirt.engine.ui.common.widget.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ovirt.engine.core.common.utils.ObjectUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasConstrainedValue;

/**
 * The CheckboxGroup Widget is used to group together a set of Checkbox buttons. Any number of checkboxes can be
 * checked/set at any point in time. Pushing/Clicking any checkbox in the group toggles its state.
 */

public class CheckBoxGroup<T> extends Composite implements TakesValue<List<T>>, HasConstrainedValue<List<T>> {

    private final Map<T, CheckBox> checkBoxes = new LinkedHashMap<>();

    private static Resources RESOURCES = GWT.create(Resources.class);
    private final FlowPanel wrapperPanel = new FlowPanel();

    private final CheckBoxGroupCss style;

    private boolean enabled = true;

    Renderer<T> renderer;

    int tabIndex;

    public interface Resources extends ClientBundle {
        @Source("org/ovirt/engine/ui/common/css/CheckBoxGroup.css")
        CheckBoxGroupCss checkBoxGroupCss();
    }

    /**
     * CheckBoxGroup construcor.
     * @param renderer
     *            to render the values passed to ListModel's setItems and hence setAcceptableValues
     */
    public CheckBoxGroup(Renderer<T> renderer) {
        this.renderer = renderer;
        style = RESOURCES.checkBoxGroupCss();
        style.ensureInjected();
        initWidget(wrapperPanel);
    }

    private void addCheckBox(T checkBoxValue) {
        String checkBoxLabel = renderer.render(checkBoxValue);
        if (checkBoxLabel == null) {
            throw new IllegalArgumentException("null value is not permited"); //$NON-NLS-1$
        }
        final CheckBox newCheckBox = buildCheckbox(checkBoxValue);
        checkBoxes.put(checkBoxValue, newCheckBox);
    }

    private CheckBox buildCheckbox(final T checkBoxValue) {
        final CheckBox newCheckBox = new CheckBox(SafeHtmlUtils.fromString(renderer.render(checkBoxValue)));
        newCheckBox.setValue(false);
        newCheckBox.setStyleName(style.checkBox());
        newCheckBox.addClickHandler(event -> {
            // ValueChangeEvent fired to notify the mapped ListModel about the new Selection/deselection.
            ValueChangeEvent.fire(CheckBoxGroup.this, getValue());
        });
        return newCheckBox;
    }

    /**
     * Clear All checkBoxes' selection in the group
     */
    public void clearAllSelections() {
        for (Entry<T, CheckBox> currentcheckBoxValue : checkBoxes.entrySet()) {
            currentcheckBoxValue.getValue().setValue(false);
        }
    }

    public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
        return addDomHandler(handler, KeyUpEvent.getType());
    }

    public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
        return addDomHandler(handler, KeyDownEvent.getType());
    }

    public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
        return addDomHandler(handler, KeyPressEvent.getType());
    }


    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enable/disable all checkboxes
     * @param enabled
     *            boolean whether to enable/disable all checkboxes
     */
    public void setEnabled(boolean enabled) {
        for(Entry<T, CheckBox> currentValue : checkBoxes.entrySet()) {
            currentValue.getValue().setEnabled(enabled);
        }
    }

    public int getTabIndex() {
        return tabIndex;
    }

    public void setTabIndex(int index) {
        tabIndex = index;
    }

    /**
     * When the mapped ListModel does a setSelectedItem, this is invoked. This method sets checked, the checkboxes
     * corresponding to the list passed to it.
     * @param value
     *            list of checkboxes to set checked.
     * @param fireEvents
     *            whether to fire ValueChangeEvent
     */
    @Override
    public void setValue(List<T> value, boolean fireEvents) {
        List<T> selectedItems = getValue();
        if (value == selectedItems || ObjectUtils.haveSameElements(selectedItems, value)) {
            return;
        }
        clearAllSelections();
        if(value == null){
            return;
        }
        for (T currentvalue : value) {
            if (checkBoxes.containsKey(currentvalue)) {
                checkBoxes.get(currentvalue).setValue(true);
            }
        }
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<T>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Api to add list of CheckBoxes to the CheckBoxGroup. This is invoked by the mapped ListModel's setItems.
     * @param values
     *            list of values for which checkboxes are to be created in the group.
     */
    @Override
    public void setAcceptableValues(Collection<List<T>> values) {
        List<T> seletedItems = getValue();
        wrapperPanel.clear();
        checkBoxes.clear();
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Widget has nothing to do");//$NON-NLS-1$
        }
        List<T> acceptableValues = (List<T>) values.toArray()[0];
        for(T currentValue : acceptableValues) {
            if(!checkBoxes.containsKey(currentValue)) {
                addCheckBox(currentValue);
            }
        }
        showCheckBoxes(seletedItems);
    }

    private void showCheckBoxes(List<T> seletedItems) {
        for (Entry<T, CheckBox> currentEntry : checkBoxes.entrySet()) {
            wrapperPanel.add(currentEntry.getValue());
            if (seletedItems.contains(currentEntry.getKey())) {
                currentEntry.getValue().setValue(true);
            }
        }
    }

    /**
     * When the mapped ListModel does a setSelectedItem, this is invoked. This method sets checked, the checkboxes
     * corresponding to the list passed to it.
     * @param value
     *            list of checkboxes to set checked.
     */
    @Override
    public void setValue(List<T> value) {
        setValue(value, false);
    }

    /**
     * Calculate and obtain the list of checkboxes checked
     * @return List of checkboxes checked
     */
    @Override
    public List<T> getValue() {
        List<T> selectedItems = new ArrayList<>();
        for (Entry<T, CheckBox> currentEntry : checkBoxes.entrySet()) {
            if (currentEntry.getValue().getValue()) {
                selectedItems.add(currentEntry.getKey());
            }
        }
        return selectedItems;
    }

}
