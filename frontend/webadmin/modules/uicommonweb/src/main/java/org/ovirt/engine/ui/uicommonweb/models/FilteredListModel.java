package org.ovirt.engine.ui.uicommonweb.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * A {@link ListModel} that allows temporarily hide some of its items.
 * @param <T> type of items
 */
public class FilteredListModel<T> extends ListModel<T> {

    /**
     * Collection if all unfiltered items.
     */
    private Collection<T> allItems;

    @Override
    public void setItems(Collection<T> value, T selectedItem) {
        super.setItems(value, selectedItem);
        if (allItems != value) {
            this.allItems = value;
        }
    }

    /**
     * It filters items in this list model. Items are shown if and only if
     * {@link org.ovirt.engine.ui.uicommonweb.models.FilteredListModel.Filter#filter(Object)} returns true.
     * Input collection for filtering is the last collection passed to {@link #setItems(java.util.Collection, Object)}
     * @param filter filter; {@code null} means 'show all'
     */
    public void filterItems(Filter<T> filter) {
        if (filter == null) {
            if (Objects.equals(this.allItems, getItems())) {
                return;
            }
            setItemsSelectionAware(this.allItems);
        }
        final Collection<T> itemsToShow = new ArrayList<>();
        if (filter != null) {
            for (T item : this.allItems) {
                if (filter.filter(item)) {
                    itemsToShow.add(item);
                }
            }
        }
        setItemsSelectionAware(itemsToShow);
    }

    private void setItemsSelectionAware(Collection<T> items) {
        final boolean preserveSelection = items.contains(getSelectedItem());
        final T selectedItem = preserveSelection ? getSelectedItem() : null;
        super.setItems(items, selectedItem);
    }

    public static interface Filter<T> {

        /**
         * @return {@code true} to show item, {@code false} to hide item
         */
        public boolean filter(T item);
    }
}
