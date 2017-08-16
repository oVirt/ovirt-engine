package org.ovirt.engine.ui.common.presenter;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMainSelectedItems<T> {
    private List<T> selectedItems;
    private final List<MainSelectedItemChangeListener<T>> registeredListeners = new ArrayList<>();

    public boolean hasSelection() {
        return selectedItems != null && !selectedItems.isEmpty();
    }

    public T getSelectedItem() {
        return hasSelection() ? selectedItems.get(0) : null;
    }

    public void registerListener(MainSelectedItemChangeListener<T> listener) {
        if (!registeredListeners.contains(listener)) {
            registeredListeners.add(listener);
        }
    }

    /**
     * Notifies this object that the main tab selection has changed.
     */
    protected void selectedItemsChanged(List<T> mainTabSelectedItems) {
        this.selectedItems = mainTabSelectedItems;

        T firstSelectedItem = getSelectedItem();

        // Notify listeners of selection change
        if (firstSelectedItem != null) {
            for (MainSelectedItemChangeListener<T> listener: registeredListeners) {
                listener.itemChanged(firstSelectedItem);
            }
        }
    }
}
