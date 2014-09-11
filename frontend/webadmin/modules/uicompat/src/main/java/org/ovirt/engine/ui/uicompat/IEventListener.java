package org.ovirt.engine.ui.uicompat;

public interface IEventListener<T extends EventArgs> {
    void eventRaised(Event<? extends T> ev, Object sender, T args);
}
