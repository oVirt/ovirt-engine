package org.ovirt.engine.ui.uicompat;

public interface IEventListener
{
    void eventRaised(Event ev, Object sender, EventArgs args);
}
