package org.ovirt.engine.ui.common.widget.listgroup;

public interface PatternflyListViewItemCreator<T> {
    PatternflyListViewItem<T> createListViewItem(T selectedItem);
}
