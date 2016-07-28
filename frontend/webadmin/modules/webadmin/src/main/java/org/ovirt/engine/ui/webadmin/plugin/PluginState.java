package org.ovirt.engine.ui.webadmin.plugin;

/**
 * Enumerates possible states of a UI plugin during the runtime.
 */
public enum PluginState {

    /**
     * The plugin has been defined through its meta-data and the corresponding iframe element has been created. This is
     * the initial state for all plugins.
     * <p>
     * Possible transitions: {@link #LOADING}
     */
    DEFINED,

    /**
     * The iframe element has been attached to DOM, with plugin host page being fetched asynchronously in the
     * background. We are now waiting for the plugin to report in as ready.
     * <p>
     * Possible transitions: {@link #READY}
     */
    LOADING,

    /**
     * The plugin has indicated that it is ready for use. We expect the event handler object (object containing plugin
     * event handler functions) to be registered at this point. We will now proceed with plugin initialization.
     * <p>
     * Possible transitions: {@link #INITIALIZING}
     */
    READY,

    /**
     * The plugin is being initialized by calling UiInit event handler function. The UiInit function will be called just
     * once during the lifetime of a plugin, before any other event handler functions are invoked by the plugin
     * infrastructure.
     * <p>
     * Possible transitions: {@link #IN_USE}, {@link #FAILED}
     */
    INITIALIZING,

    /**
     * Plugin's UiInit event handler function has completed successfully, we can now call other event handler functions
     * as necessary. The plugin is in use now.
     * <p>
     * Possible transitions: {@link #FAILED}
     */
    IN_USE,

    /**
     * An uncaught exception escaped while calling an event handler function, which indicates internal error within the
     * plugin code. The iframe element has been detached from DOM. The plugin is removed from service.
     * <p>
     * Possible transitions: N/A (end state)
     */
    FAILED

}
