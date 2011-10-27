package org.ovirt.engine.core.compat;

public final class ProvidePropertyChangedEvent
{
    public static EventDefinition Definition;

    static
    {
        Definition = new EventDefinition("PropertyChanged", IProvidePropertyChangedEvent.class);
    }
}
