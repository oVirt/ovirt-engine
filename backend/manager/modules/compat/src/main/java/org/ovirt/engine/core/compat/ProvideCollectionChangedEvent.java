package org.ovirt.engine.core.compat;

public final class ProvideCollectionChangedEvent
{
    public static final EventDefinition Definition;

    static
    {
        Definition = new EventDefinition("CollectionChanged", IProvideCollectionChangedEvent.class);
    }
}
