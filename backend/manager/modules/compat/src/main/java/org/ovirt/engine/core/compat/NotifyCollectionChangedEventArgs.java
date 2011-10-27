package org.ovirt.engine.core.compat;

public class NotifyCollectionChangedEventArgs extends EventArgs {

    public NotifyCollectionChangedAction Action;
    public Iterable<Object> NewItems;
    public Iterable<Object> OldItems;
}
