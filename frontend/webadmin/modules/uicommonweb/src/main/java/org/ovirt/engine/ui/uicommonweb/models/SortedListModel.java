package org.ovirt.engine.ui.uicommonweb.models;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

public class SortedListModel extends ListModel {

    private final Comparator<?> comparator;

    public SortedListModel(Comparator<?> comparator) {
        this.comparator = comparator;
    }

    @Override
    public void setItems(Iterable value) {
        SortedSet items = new TreeSet(comparator);
        for (Object item : value) {
            items.add(item);
        }

        super.setItems(items);
    }

}
