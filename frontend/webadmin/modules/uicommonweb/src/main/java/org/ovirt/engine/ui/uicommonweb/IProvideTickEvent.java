package org.ovirt.engine.ui.uicommonweb;

import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

public interface IProvideTickEvent {
    Event<EventArgs> getTickEvent();
}
