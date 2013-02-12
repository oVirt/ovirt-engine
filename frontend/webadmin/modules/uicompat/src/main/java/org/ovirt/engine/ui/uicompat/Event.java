package org.ovirt.engine.ui.uicompat;

import org.ovirt.engine.core.compat.StringHelper;

public class Event {
    private java.util.List<IEventListener> listeners;
    private java.util.Map<IEventListener, Object> contexts;
    private java.lang.Class privateOwnerType;
    public java.lang.Class getOwnerType()
    {
        return privateOwnerType;
    }
    private void setOwnerType(java.lang.Class value)
    {
        privateOwnerType = value;
    }
    private String privateName;
    public String getName()
    {
        return privateName;
    }
    private void setName(String value)
    {
        privateName = value;
    }

    /**
     Gets an object representing current event context.
     Specified when add listener.
    */
    private Object privateContext;
    public Object getContext()
    {
        return privateContext;
    }
    private void setContext(Object value)
    {
        privateContext = value;
    }


    public Event(String name, java.lang.Class ownerType)
    {
        setName(name);
        setOwnerType(ownerType);

        listeners = new java.util.ArrayList<IEventListener>();
        contexts = new java.util.HashMap<IEventListener, Object>();
    }

    public Event()
    {
    }

    public Event(EventDefinition definition)
    {
        this(definition.getName(), definition.getOwnerType());
    }

    /**
     Add listener with no context specified.
    */
    public void addListener(IEventListener listener)
    {
        listeners.add(listener);
    }

    public void addListener(IEventListener listener, Object context)
    {
        listeners.add(listener);
        contexts.put(listener, context);
    }

    public void removeListener(IEventListener listener)
    {
        listeners.remove(listener);

        if (contexts.containsKey(listener))
        {
            contexts.remove(listener);
        }
    }

    public void raise(Object sender, EventArgs e)
    {
        //Iterate on a new instance of listeners list,
        //to enable listener unsubscribe from event
        //as a result on event fairing.
        java.util.ArrayList<IEventListener> list = new java.util.ArrayList<IEventListener>();
        for (IEventListener listener : listeners)
        {
            list.add(listener);
        }

        for (IEventListener listener : list)
        {
            //Update current context.
            setContext(contexts.containsKey(listener) ? contexts.get(listener) : null);

            listener.eventRaised(this, sender, e);
        }
    }

    public boolean equals(Event other) {
        if(other == null){
            return false;
        }
        return StringHelper.stringsEqual(getName(), other.getName())
        && getOwnerType() == other.getOwnerType();
    }

    public boolean equals(EventDefinition other) {
        if(other == null){
            return false;
        }
        return StringHelper.stringsEqual(getName(), other.getName())
        && getOwnerType() == other.getOwnerType();
    }

    public java.util.List<IEventListener> getListeners() {
        return listeners;
    }

    public void setListeners(java.util.List<IEventListener> listeners) {
        this.listeners = listeners;
    }
}
