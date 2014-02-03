package org.ovirt.engine.core.notifier.transport;

import org.ovirt.engine.core.notifier.dao.DispatchResult;

public interface Observable {

    void notifyObservers(DispatchResult data);

    void registerObserver(Observer observer);

    void removeObserver(Observer observer);
}
