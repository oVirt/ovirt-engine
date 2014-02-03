package org.ovirt.engine.core.notifier.transport;

import org.ovirt.engine.core.notifier.dao.DispatchResult;

public interface Observer {

    void update(Observable o, DispatchResult data);
}
