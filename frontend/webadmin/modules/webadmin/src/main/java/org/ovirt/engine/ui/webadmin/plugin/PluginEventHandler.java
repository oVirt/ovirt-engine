package org.ovirt.engine.ui.webadmin.plugin;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

/**
 * Handles WebAdmin application events (extension points) to be consumed by UI plugins.
 * <p>
 * Should be bound as GIN eager singleton, created early on during application startup.
 */
public class PluginEventHandler {

    @Inject
    public PluginEventHandler(EventBus eventBus, PluginManager manager) {
        // TODO call EventBus.addHandler for each extension point (event),
        // with the handler implementation using PluginManager to call plugins
    }

}
