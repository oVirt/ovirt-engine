package org.ovirt.engine.ui.common.presenter;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMainTabSelectedItems<T> {
    private List<T> selectedItems;
    private final List<MainTabSelectedItemChangeListener<T>> registeredListeners = new ArrayList<>();

    public boolean hasSelection() {
        return selectedItems != null && !selectedItems.isEmpty();
    }

    public T getSelectedItem() {
        return hasSelection() ? selectedItems.get(0) : null;
    }

    public void registerListener(MainTabSelectedItemChangeListener<T> listener) {
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
            for (MainTabSelectedItemChangeListener<T> listener: registeredListeners) {
                listener.itemChanged(firstSelectedItem);
            }
        }
    }
}
