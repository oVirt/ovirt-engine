package org.ovirt.engine.ui.uicompat;


public class EventProvidingCollection<T> extends ObservableCollection<T> implements IProvideCollectionChangedEvent
{
	private Event privateCollectionChangedEvent;
	public Event getCollectionChangedEvent()
	{
		return privateCollectionChangedEvent;
	}
	private void setCollectionChangedEvent(Event value)
	{
		privateCollectionChangedEvent = value;
	}

	public EventProvidingCollection()
	{
		setCollectionChangedEvent(new Event(ProvideCollectionChangedEvent.Definition));
	}

	@Override
	protected void OnCollectionChanged(NotifyCollectionChangedEventArgs e)
	{
		super.OnCollectionChanged(e);
		getCollectionChangedEvent().raise(this, e);
	}
}
