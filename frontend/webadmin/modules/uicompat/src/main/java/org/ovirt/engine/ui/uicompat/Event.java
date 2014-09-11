package org.ovirt.engine.ui.uicompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Event<T extends EventArgs> {
    private List<IEventListener<? super T>> listeners;
    private Map<IEventListener<? super T>, Object> contexts;
    private Class<?> privateOwnerType;
    public Class<?> getOwnerType()
    {
        return privateOwnerType;
    }
    private void setOwnerType(Class<?> value)
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


    public Event(String name, Class<?> ownerType)
    {
        setName(name);
        setOwnerType(ownerType);

        listeners = new ArrayList<IEventListener<? super T>>();
        contexts = new HashMap<IEventListener<? super T>, Object>();
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
    public void addListener(IEventListener<? super T> listener)
    {
        listeners.add(listener);
    }

    public void addListener(IEventListener<? super T> listener, Object context)
    {
        listeners.add(listener);
        contexts.put(listener, context);
    }

    public void removeListener(IEventListener<? super T> listener)
    {
        listeners.remove(listener);

        if (contexts.containsKey(listener))
        {
            contexts.remove(listener);
        }
    }

    public void raise(Object sender, T e)
    {
        //Iterate on a new instance of listeners list,
        //to enable listener unsubscribe from event
        //as a result on event fairing.
        ArrayList<IEventListener<? super T>> list = new ArrayList<IEventListener<? super T>>();
        for (IEventListener<? super T> listener : listeners)
        {
            list.add(listener);
        }

        for (IEventListener<? super T> listener : list)
        {
            //Update current context.
            setContext(contexts.containsKey(listener) ? contexts.get(listener) : null);

            listener.eventRaised(this, sender, e);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((privateName == null) ? 0 : privateName.hashCode());
        result = prime * result + ((privateOwnerType == null) ? 0 : privateOwnerType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Event<?> other = (Event<?>) obj;
        if (privateName == null) {
            if (other.privateName != null)
                return false;
        } else if (!privateName.equals(other.privateName))
            return false;
        if (privateOwnerType == null) {
            if (other.privateOwnerType != null)
                return false;
        } else if (!privateOwnerType.equals(other.privateOwnerType))
            return false;
        return true;
    }

    public boolean matchesDefinition(EventDefinition other) {
        if(other == null){
            return false;
        }
        return getName().equals(other.getName())
        && getOwnerType() == other.getOwnerType();
    }

    public List<IEventListener<? super T>> getListeners() {
        return listeners;
    }

    public void setListeners(List<IEventListener<? super T>> listeners) {
        this.listeners = listeners;
    }
}
