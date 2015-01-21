package org.ovirt.engine.ui.common.widget.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
 * The CheckboxGroup Widget is used to group together a set of Checkbox buttons. By default first checkbox is checked
 * due to default behaviour of UiCommonEditorVisitor. Use clearAllSelections to deselect all checkboxes. Any number of
 * checkboxes can be checked/set at any point in time. Pushing/Clicking any checkbox in the group toggles its state.
 */

public class CheckBoxGroup<T> extends Composite implements TakesValue<List<T>>, HasConstrainedValue<List<T>> {

    private final Map<T, CheckBox> checkBoxes = new LinkedHashMap<>();

    private static Resources RESOURCES = GWT.create(Resources.class);
    private final FlowPanel wrapperPanel = new FlowPanel();

    private CheckBoxGroupCss style;

    private boolean enabled = true;

    Renderer<T> renderer;

    int tabIndex;

    public interface Resources extends ClientBundle {
        @Source("org/ovirt/engine/ui/common/css/CheckBoxGroup.css")
        CheckBoxGroupCss checkBoxGroupCss();
    }

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
        newCheckBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ValueChangeEvent.fire(CheckBoxGroup.this, getValue());
            }
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

    @Override
    public void setValue(List<T> value, boolean fireEvents) {
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

    @Override
    public void setAcceptableValues(Collection<List<T>> values) {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Widget has nothing to do");//$NON-NLS-1$
        }
        List<T> acceptableValues = (List<T>) values.toArray()[0];
        for(T currentValue : acceptableValues) {
            if(!checkBoxes.containsKey(currentValue)) {
                addCheckBox(currentValue);
            }
        }
        showCheckBoxes();
    }

    private void showCheckBoxes() {
        for (Entry<T, CheckBox> currentEntry : checkBoxes.entrySet()) {
            wrapperPanel.add(currentEntry.getValue());
        }
    }

    @Override
    public void setValue(List<T> value) {
        setValue(value, false);
    }

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
