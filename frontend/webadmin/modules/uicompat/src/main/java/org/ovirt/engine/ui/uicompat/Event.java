package org.ovirt.engine.ui.uicompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * The main Event class in uicommon's eventing infrastructure. Note that this is
 * completely separate from GWT's eventing infrastructure, GwtEvent, etc.
 * </p>
 * <p>
 * Encapsulates an event that occurs to an object -- usually a Model object. The
 * event can be anything, but is typically a property value change.
 * </p>
 * <p>
 * Event uses a simple publish-subscribe model. Create an Event, and set it as a
 * Model field. Subscribe to the Event by calling addListener() (usually do this
 * from a Presenter or View). Event keeps the subscribers in a simple List.
 * Fire the event (probably in a Model setter) when your event condition occurs
 * (for example, a Model property is changed) by calling Event.raise(). raise()
 * publishes the event to all those who subscribed via addListener();
 * </p>
 */
public class Event<T extends EventArgs> {

    private List<IEventListener<? super T>> listeners;
    private Map<IEventListener<? super T>, Object> contexts;
    private Class<?> privateOwnerType;
    public Class<?> getOwnerType() {
        return privateOwnerType;
    }
    private void setOwnerType(Class<?> value) {
        privateOwnerType = value;
    }
    private String privateName;
    public String getName() {
        return privateName;
    }
    private void setName(String value) {
        privateName = value;
    }

    /**
     * Gets an object representing current event context. Specified when add listener.
     */
    private Object privateContext;
    public Object getContext() {
        return privateContext;
    }
    private void setContext(Object value) {
        privateContext = value;
    }


    public Event(String name, Class<?> ownerType) {
        setName(name);
        setOwnerType(ownerType);

        listeners = new ArrayList<>();
        contexts = new HashMap<>();
    }

    public Event() {
    }

    public Event(EventDefinition definition) {
        this(definition.getName(), definition.getOwnerType());
    }

    /**
     * Add listener with no context specified.
     */
    public void addListener(IEventListener<? super T> listener) {
        listeners.add(listener);
    }

    /**
     * Subscribe to this Event. Subscriber will have eventRaised() called back when the event is published.
     */
    public void addListener(IEventListener<? super T> listener, Object context) {
        listeners.add(listener);
        contexts.put(listener, context);
    }

    public void removeListener(IEventListener<? super T> listener) {
        listeners.remove(listener);

        if (contexts.containsKey(listener)) {
            contexts.remove(listener);
        }
    }

    /**
     * Raise (publish) the event. This simply calls eventRaised() on all those who subscribed via addListener();
     */
    public void raise(Object sender, T e) {
        // Iterate on a new instance of listeners list,
        // to enable listener unsubscribe from event
        // as a result on event firing.

        ArrayList<IEventListener<? super T>> list = new ArrayList<>();

        for (IEventListener<? super T> listener : listeners) {
            list.add(listener);

        }

        for (IEventListener<? super T> listener : list) {
            //Update current context.
            setContext(contexts.containsKey(listener) ? contexts.get(listener) : null);

            listener.eventRaised(this, sender, e);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                privateName,
                privateOwnerType
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Event)) {
            return false;
        }
        Event<?> other = (Event<?>) obj;
        return Objects.equals(privateName, other.privateName)
                && Objects.equals(privateOwnerType, other.privateOwnerType);
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

    public void clearListeners() {
        listeners.clear();
        contexts.clear();
    }

    public void setListeners(List<IEventListener<? super T>> listeners) {
        this.listeners = listeners;
    }
}
