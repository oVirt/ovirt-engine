package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

public interface ISpice extends ConsoleClient {

    Event<EventArgs> getDisconnectedEvent();
    Event<EventArgs> getConnectedEvent();
    Event<EventArgs> getMenuItemSelectedEvent();

}
