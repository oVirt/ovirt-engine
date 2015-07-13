package org.ovirt.engine.ui.uicommonweb;

import org.ovirt.engine.ui.uicompat.EventDefinition;

@SuppressWarnings("unused")
public final class ProvideTickEvent {
    public static final EventDefinition definition;

    static {
        definition = new EventDefinition("Tick", IProvideTickEvent.class); //$NON-NLS-1$
    }
}
