package org.ovirt.engine.ui.uicommonweb.models;

import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

public class SortedListModel<T> extends ListModel<T> {

    private final Comparator<? super T> comparator;

    public SortedListModel(Comparator<? super T> comparator) {
        this.comparator = comparator;
    }

    @Override
    public void setItems(Collection<T> value) {
        SortedSet<T> items = new TreeSet<T>(comparator);
        items.addAll(value);

        super.setItems(items);
    }

}
