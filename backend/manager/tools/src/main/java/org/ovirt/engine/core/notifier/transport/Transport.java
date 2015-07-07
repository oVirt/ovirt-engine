package org.ovirt.engine.core.notifier.transport;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.notifier.dao.DispatchResult;
import org.ovirt.engine.core.notifier.filter.AuditLogEvent;

public abstract class Transport implements Observable {

    private Set<Observer> observers = new HashSet<>();

    /**
     *
     * @return the name for this transport
     */
    public abstract String getName();

    /**
     *
     * @return true if active.
     */
    public abstract boolean isActive();

    /**
     * Dispatches event to an address.
     *
     * @param event
     *            the event to dispatch
     * @param address
     *            an address understood by this transport
     */
    public abstract void dispatchEvent(AuditLogEvent event, String address);

    /**
     * Upon an idle call a transport performs background tasks if needed.
     * A default empty implementation is provided.
     */
    public void idle() {
    }

    @Override
    public void notifyObservers(DispatchResult data) {
        for (Observer observer : observers) {
            observer.update(this, data);
        }
    }

    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }
}
