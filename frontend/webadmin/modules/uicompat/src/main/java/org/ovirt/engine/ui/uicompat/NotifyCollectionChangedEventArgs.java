package org.ovirt.engine.ui.uicompat;

public class NotifyCollectionChangedEventArgs<T> extends EventArgs {

    public Iterable<T> newItems;
    public Iterable<T> oldItems;
}
