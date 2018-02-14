package org.ovirt.engine.ui.uicommonweb.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel.AbstractSelectionModel;

/**
 * Selection model that allows multiple items to be selected, while preserving selection order.
 *
 * @param <T> Table row data type.
 * @see com.google.gwt.view.client.MultiSelectionModel
 */
public class OrderedMultiSelectionModel<T> extends AbstractSelectionModel<T> {

    // Selected items mapped by their keys
    private final Map<Object, T> selectedSet = new LinkedHashMap<>();

    // Selection changes to be resolved
    private final Map<T, Boolean> selectionChanges = new LinkedHashMap<>();

    private boolean multiSelectEnabled;
    private boolean multiRangeSelectEnabled;
    private HasData<T> dataDisplay;
    private int lastSelectedRow = -1;
    private int originSelectedRow = -1;
    private final Set<Integer> disabledRows = new HashSet<>();

    public OrderedMultiSelectionModel() {
        super(null);
    }

    public OrderedMultiSelectionModel(ProvidesKey<T> keyProvider) {
        super(keyProvider);
    }

    /**
     * Turns multiple selection feature on or off.
     */
    public void setMultiSelectEnabled(boolean multiSelectEnabled) {
        this.multiSelectEnabled = multiSelectEnabled;
    }

    public boolean isMultiSelectEnabled() {
        return multiSelectEnabled;
    }

    /**
     * Turns multiple 'range' selection feature on or off.
     */
    public void setMultiRangeSelectEnabled(boolean multiRangeSelectEnabled) {
        this.multiRangeSelectEnabled = multiRangeSelectEnabled;
    }

    public boolean isMultiRangeSelectEnabled() {
        return multiRangeSelectEnabled;
    }

    /**
     * Deselects all selected values.
     */
    public void clear() {
        clearSelection();
        scheduleSelectionChangeEvent();
    }

    void clearSelection() {
        selectionChanges.clear();

        for (T value : selectedSet.values()) {
            selectionChanges.put(value, false);
        }
    }

    /**
     * Returns the list of selected items as a copy.
     */
    public List<T> getSelectedList() {
        resolveChanges();
        return new ArrayList<>(selectedSet.values());
    }

    @Override
    public boolean isSelected(T object) {
        resolveChanges();
        return selectedSet.containsKey(getKey(object));
    }

    @Override
    public void setSelected(T object, boolean selected) {
        if (multiSelectEnabled) {
            selectionChanges.put(object, !isSelected(object));
            originSelectedRow = -1;
        } else if (multiRangeSelectEnabled) {
            selectRange(object);
        } else {
            clearSelection();
            selectionChanges.put(object, selected);
            originSelectedRow = -1;
        }

        // Save last selected row index
        lastSelectedRow = getRowIndexByObject(object);

        scheduleSelectionChangeEvent();
    }

    @Override
    protected void fireSelectionChangeEvent() {
        if (isEventScheduled()) {
            setEventCancelled(true);
        }
        resolveChanges();
    }

    public void resolveChanges() {
        Set<Object> selectedKeys = selectedSet.keySet();
        List<Object> visibleKeys = new ArrayList<>();
        for (T visible : dataDisplay.getVisibleItems()) {
            visibleKeys.add(getKey(visible));
        }

        if (!visibleKeys.containsAll(selectedKeys)) {
            for (Map.Entry<Object, T> selectedEntry : selectedSet.entrySet()) {
                if (!visibleKeys.contains(selectedEntry.getKey())) {
                    selectionChanges.put(selectedEntry.getValue(), false);
                }
            }
        }

        if (selectionChanges.isEmpty()) {
            return;
        }

        boolean changed = false;
        for (Map.Entry<T, Boolean> entry : selectionChanges.entrySet()) {
            T object = entry.getKey();
            boolean selected = entry.getValue();

            Object key = getKey(object);
            T oldValue = selectedSet.get(key);
            if (selected) {
                if (oldValue == null || !oldValue.equals(object)) {
                    selectedSet.put(getKey(object), object);
                    changed = true;
                }
            } else {
                if (oldValue != null) {
                    selectedSet.remove(key);
                    changed = true;
                }
            }
        }
        selectionChanges.clear();

        if (changed) {
            SelectionChangeEvent.fire(this);
        }
    }

    // Select a range of multiple rows
    private void selectRange(T object) {
        // Select multiple selection origin row
        if (originSelectedRow == -1) {
            originSelectedRow = lastSelectedRow;
        }

        // Get start/end rows
        int selectedRow = getRowIndexByObject(object);
        int startRow = originSelectedRow < selectedRow ? originSelectedRow : selectedRow;
        int endRow = originSelectedRow > selectedRow ? originSelectedRow : selectedRow;

        int lastIndex = visibleItemsAsList().size() - 1;
        //Adjust the end row in cases where multiple items were deleted
        endRow = endRow > lastIndex ? lastIndex : endRow;

        // Clear current selection and select row in range
        clearSelection();
        for (int row = startRow; row <= endRow; row++) {
            selectionChanges.put(visibleItemsAsList().get(row), true);
        }
    }

    // Get row's index by a specified row object
    private int getRowIndexByObject(T object) {
        for (int row = 0; row < dataDisplay.getRowCount(); row++) {
            if (visibleItemsAsList().get(row).equals(object)) {
                return row;
            }
        }

        return -1;
    }

    private List<T> visibleItemsAsList() {
        List<T> result = new ArrayList<>();
        dataDisplay.getVisibleItems().iterator().forEachRemaining(result::add);
        return result;
    }

    // Select a row with regarding a specified shift
    private void selectRow(int shift) {
        if (selectedSet.isEmpty() || dataDisplay == null) {
            return;
        }

        int shiftSelectedRow = lastSelectedRow + shift;
        int nextRow =
                shiftSelectedRow >= 0  && dataDisplay.getRowCount() != 0 ?
                        shiftSelectedRow % dataDisplay.getRowCount() : shiftSelectedRow + dataDisplay.getRowCount();

        if (disabledRows.contains(nextRow)) {
            selectRow(shift > 0 ? shift + 1 : shift - 1);
            return;
        }

        setSelected(visibleItemsAsList().get(nextRow), true);
    }

    public void setDisabledRows(int... disabledRows) {
        if (disabledRows != null) {
            for (int i : disabledRows) {
                this.disabledRows.add(i);
            }
        }
    }

    public int getLastSelectedRow() {
        return lastSelectedRow;
    }

    public void selectNext() {
        selectRow(1);
    }

    public void selectPrev() {
        selectRow(-1);
    }

    public void selectAllPrev() {
        selectRange(firstItem());
        scheduleSelectionChangeEvent();
    }

    public void selectAllNext() {
        selectRange(lastItem());
        scheduleSelectionChangeEvent();
    }

    public void selectAll() {
        clearSelection();
        setMultiRangeSelectEnabled(false);
        setMultiSelectEnabled(false);
        setSelected(firstItem(), true);
        selectRange(lastItem());
        scheduleSelectionChangeEvent();
    }

    private T firstItem() {
        return visibleItemsAsList().get(0);
    }

    private T lastItem() {
        List<T> visibleItems = visibleItemsAsList();
        return visibleItems.get(visibleItems.size() - 1);
    }

    public void setDataDisplay(HasData<T> dataDisplay) {
        this.dataDisplay = dataDisplay;
    }

}
