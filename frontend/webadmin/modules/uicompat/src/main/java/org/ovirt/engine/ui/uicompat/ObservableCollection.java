package org.ovirt.engine.ui.uicompat;

import java.util.ArrayList;
import java.util.Collection;


public class ObservableCollection<T> extends ArrayList<T> implements IProvideCollectionChangedEvent {

    private Event collectionChangedEvent;

    public ObservableCollection() {
        setCollectionChangedEvent(new Event(ProvideCollectionChangedEvent.Definition));
    }

    /** Moves a collection element from the index sourceIndex to the index destIndex
     * @param sourceIndex
     * @param destIndex
     */
    public void Move(int sourceIndex, int destIndex) {
        if (sourceIndex == destIndex || sourceIndex > this.size() || destIndex > this.size()) {
            return;
        }

        T tempObj = this.get(sourceIndex);
        this.remove(sourceIndex);
        this.add(destIndex, tempObj);
    }

    protected void OnCollectionChanged() {
        OnCollectionChanged(new NotifyCollectionChangedEventArgs());
    }

    protected void OnCollectionChanged(NotifyCollectionChangedEventArgs e) {
        collectionChangedEvent.raise(this, e);
    }

    @Override
    public Event getCollectionChangedEvent() {
        return collectionChangedEvent;
    }

    private void setCollectionChangedEvent(Event collectionChangedEvent) {
        this.collectionChangedEvent = collectionChangedEvent;
    }


    @Override
    public boolean add(T e) {
        boolean b = super.add(e);
        OnCollectionChanged();
        return b;
    }

    @Override
    public void add(int index, T element) {
        super.add(index, element);
        OnCollectionChanged();
    }

    @Override
    public T set(int index, T element) {
        T t = super.set(index, element);
        OnCollectionChanged();
        return t;
    }

    @Override
    public boolean remove(Object o) {
        boolean b = super.remove(o);
        OnCollectionChanged();
        return b;
    }


    public void clear() {
        super.clear();
        OnCollectionChanged();
    }

    public boolean addAll(Collection<? extends T> c) {
        boolean b = super.addAll(c);
        OnCollectionChanged();
        return b;
    }

    public boolean addAll(int index, Collection<? extends T> c) {
        boolean b = super.addAll(index, c);
        OnCollectionChanged();
        return b;
    }

}

