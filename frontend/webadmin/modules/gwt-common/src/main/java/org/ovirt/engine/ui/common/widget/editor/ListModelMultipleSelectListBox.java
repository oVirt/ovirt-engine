package org.ovirt.engine.ui.common.widget.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ListBox;

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
     * Internal list that contains the typed values so we can look them up based on the selected indexes of the
     * {@code ListBox}.
     */
    private List<T> typedItemList = new ArrayList<>();

    /**
     * The current selected list of typed values.
     */
    private List<T> selectedList = new ArrayList<>();

    /**
     * Constructor
     * @param renderer The rendered used to render the values.
     */
    public ListModelMultipleSelectListBox(Renderer<List<T>> renderer) {
        super(renderer);
        asListBox().setMultipleSelect(true);
    }

    @Override
    public void setValue(List<T> value, boolean fireEvents) {
        List<T> originalValues = new ArrayList<>(selectedItems());
        updateSelectedList();
        if (fireEvents) {
            ValueChangeEvent.fireIfNotEqual(this, originalValues, selectedItems());
        }
    }

    @Override
    public void setListValue(List<T> value) {
        // Reset the current selected values, we will locate the right selected values below.
        selectedList.clear();
        selectedList.addAll(value);
        ListBox listBox = asListBox();
        //Unselect anything set by the 'normal' setValue
        if (listBox.getSelectedIndex() > -1) {
            listBox.setItemSelected(listBox.getSelectedIndex(), false);
        }
        for (T item: selectedList) {
            int itemIndex = typedItemList.indexOf(item);
            if (itemIndex > -1 && itemIndex < listBox.getItemCount()) {
                listBox.setItemSelected(itemIndex, true);
            }
        }
    }

    /**
     * Update the current selected list based on the state of the actual {@code ListBox}
     */
    private void updateSelectedList() {
        Set<Integer> indexes = new HashSet<>();
        // Reset the current selected values, we will locate the right selected values below.
        selectedList.clear();
        ListBox listBox = asListBox();
        // There is no method available in a ListBox to find all the selected items if multi select
        // is enabled. The suggested method is looping over each item and checking if they are selected.
        // This attempts to optimize the loop by using the getSelectedIndex() to get the first selected
        // item and deselecting it, then at the end re-select them all. This avoids the loop.
        while (listBox.getSelectedIndex() >= 0) {
            int index = listBox.getSelectedIndex();
            listBox.setItemSelected(index, false);
            if (index >= 0 && index < typedItemList.size()) {
                T value = typedItemList.get(index);
                selectedList.add(value);
            }
            // Mark the index, so we can reselect at the end.
            indexes.add(index);
        }
        // Put back the selection.
        for (Integer index : indexes) {
            listBox.setItemSelected(index, true);
        }
    }

    /**
     * Stored the typed value in an internal data structure so we can look it up based on the index.
     * @param value The single valued list containing the value.
     */
    private void addToItems(List<T> value) {
        typedItemList.addAll(value);
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
        List<T> val = newValues.isEmpty() ? Collections.<T>emptyList() :  newValues.iterator().next();
        super.setValue(val, false);
        // Populate the list box.
        super.setAcceptableValues(newValues);
        // Store the rendered values so we can reverse them to find the right typed value. This will
        // break if more than one type value renders to the same string.
        typedItemList.clear();
        for (List<T> value : newValues) {
            addToItems(value);
        }
    }

    /**
     * Getter for the list of selected items.
     * @return The list of typed selected items.
     */
    public List<T> selectedItems() {
        return selectedList;
    }
}
