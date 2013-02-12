package org.ovirt.engine.ui.uicompat;

public class NotifyCollectionChangedEventArgs extends EventArgs {

    public NotifyCollectionChangedAction Action;
    public Iterable<Object> NewItems;
    public Iterable<Object> OldItems;
}
