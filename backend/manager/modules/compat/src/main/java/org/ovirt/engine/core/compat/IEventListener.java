package org.ovirt.engine.core.compat;

public interface IEventListener
{
    void eventRaised(Event ev, Object sender, EventArgs args);
}
