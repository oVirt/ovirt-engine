package org.ovirt.engine.ui.uicommonweb;

import org.ovirt.engine.core.compat.EventDefinition;

@SuppressWarnings("unused")
public final class ProvideTickEvent
{
    public static EventDefinition Definition;

    static
    {
        Definition = new EventDefinition("Tick", IProvideTickEvent.class);
    }
}
