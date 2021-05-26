package org.ovirt.engine.ui.common.widget.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.compat.StringHelper;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.Renderer;

/**
 * See ListModelMultipleSelectListBoxEditor for an explanation of how to use the ListBox
 * @see ListModelMultipleSelectListBoxEditor
 */
public class ListModelMultipleSelectListBox<T> extends ListModelListBox<List<T>> implements TakesListValue<T> {
    /**
     * The editor.
     */
    private TakesConstrainedValueListEditor<T> editor;

    /**
     * The current selected list of typed values.
     */
    protected List<T> selectedList = new ArrayList<>();

    protected List<T> lastValues;

    /**
     * Constructor
     * @param renderer The rendered used to render the values.
     */
    public ListModelMultipleSelectListBox(Renderer<List<T>> renderer) {
        super(renderer);
        this.isMultiSelect = true;
    }

    @Override
    protected void setValue(List<T> values, boolean fireEvents, boolean fromClick) {
        // Prevent potential event loops, as well as optimize away multiple set values to the same value.
        if (values != null && lastValues != null && lastValues.equals(values)) {
            return;
        }

        // Create new array instance for ListModel::setSelectedItems equal check.
        selectedList = new ArrayList<>(selectedList);
        if (fromClick) {
            // Click event can only be one value;
            T value = values.get(0);
            if (selectedList.contains(value)) {
                // Can only remove 1 at a time, so if we get more than 1 we shouldn't remove them.
                selectedList.remove(value);
            } else {
                // Shouldn't get a null value, but in case we do, ignore it.
                if (value != null) {
                    selectedList.add(value);
                }
            }
        } else {
            // Received a list of selections from some code, we should clear the selected items, and replace them
            // with the ones received.
            selectedList.clear();
            if (values != null) {
                for (T value: values) {
                    if (value != null) {
                        selectedList.add(value);
                    }
                }
            }
        }
        listPanel.setSelected(selectedList);
        updateCurrentValue(selectedList, fireEvents);
    }

    private void updateCurrentValue(final List<T> value, boolean fireEvents) {
        lastValues = value;
        List<String> renderedValues = new ArrayList<>();
        for (T val: value) {
            renderedValues.add(getRenderer().render(Arrays.asList(val)));
        }
        String renderedValue = String.join(",", renderedValues); //$NON-NLS-1$
        if (StringHelper.isNullOrEmpty(renderedValue)) {
            renderedValue = NBSP;
        } else {
            renderedValue = SafeHtmlUtils.htmlEscape(renderedValue);
        }
        ((Element)dropdownButton.getElement().getChild(0)).setInnerHTML(renderedValue);
        dropdownButton.setTitle(renderedValue);
        Scheduler.get().scheduleDeferred(() -> listPanel.setSelected(value));
        if (fireEvents) {
            Scheduler.get().scheduleDeferred(() -> {
                ValueChangeEvent.fire(ListModelMultipleSelectListBox.this, selectedItems());
                // Clear the value so we don't have any leaks
                lastValues = null;
            });
        } else {
            // Clear the value so we don't have any leaks
            lastValues = null;
        }
    }

    @Override
    public void setListValue(List<T> value) {
        setValue(value, true);
    }

    @Override
    public TakesConstrainedValueListEditor<T> asEditor() {
        if (editor == null) {
            editor = TakesConstrainedValueListEditor.ofList(this, this, this);
        }
        return editor;
    }

    @Override
    public void setAcceptableValues(Collection<List<T>> newValues) {
        if (newValues == null) {
            newValues = Collections.emptyList();
        }
        // Set a value in the super class, so calling setAcceptableValues doesn't add a null value and
        // potentially NPE if the renderer doesn't take kindly to getting a null value passed to it.
        List<T> val = newValues.isEmpty() ? Collections.emptyList() : newValues.iterator().next();

        //Clone selection.
        List<T> currentSelected = new ArrayList<>();
        for (T value: selectedItems()) {
            if (val.contains(value)) {
                currentSelected.add(value);
            }
        }
        selectedList.clear();
        // Populate the list box.
        super.setAcceptableValues(newValues);
        setValue(currentSelected, false);
    }

    /**
     * Getter for the list of selected items.
     * @return The list of typed selected items.
     */
    public List<T> selectedItems() {
        return selectedList;
    }
}
