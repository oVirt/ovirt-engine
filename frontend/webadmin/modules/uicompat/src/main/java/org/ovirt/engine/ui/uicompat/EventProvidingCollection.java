package org.ovirt.engine.ui.uicompat;


public class EventProvidingCollection<T> extends ObservableCollection<T> implements IProvideCollectionChangedEvent
{
    private Event<EventArgs> privateCollectionChangedEvent;
    public Event<EventArgs> getCollectionChangedEvent()
    {
        return privateCollectionChangedEvent;
    }
    private void setCollectionChangedEvent(Event<EventArgs> value)
    {
        privateCollectionChangedEvent = value;
    }

    public EventProvidingCollection()
    {
        setCollectionChangedEvent(new Event<>(ProvideCollectionChangedEvent.Definition));
    }

    @Override
    protected void onCollectionChanged(NotifyCollectionChangedEventArgs e)
    {
        super.onCollectionChanged(e);
        getCollectionChangedEvent().raise(this, e);
    }
}
